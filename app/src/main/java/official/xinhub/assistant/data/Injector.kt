package official.xinhub.assistant.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * Applies a script archive onto the game asset tree.
 *
 * Every file that is about to be replaced is copied into app-private storage first, keyed by
 * job id, so a restore never depends on the game redownloading anything.
 */
class Injector(private val ctx: Context, private val prefs: Prefs) {

    private val zipDir: File get() = File(ctx.cacheDir, "zips").apply { mkdirs() }
    private val backupRoot: File get() = File(ctx.filesDir, "backups").apply { mkdirs() }

    data class Progress(
        val running: Boolean = false,
        val done: Int = 0,
        val total: Int = 0,
        val label: String = "",
        val error: String? = null,
        val finished: Boolean = false,
        val phase: String = "preparing",
        val bytes: Long = 0L,
        val bytesTotal: Long = 0L,
    )

    private fun zipPath(job: Job): File {
        val name = job.script.substringAfterLast('/').substringBefore('?').ifBlank { "script.zip" }
        val hash = job.script.hashCode().toUInt().toString(16)
        return File(zipDir, "${hash}_${name.takeLast(48)}")
    }

    suspend fun fetch(
        job: Job,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): File? = withContext(Dispatchers.IO) {
        if (job.script.startsWith("file:")) {
            val path = job.script.removePrefix("file://")
            val local = File(path)
            return@withContext if (local.exists() && local.length() > 0) local else null
        }
        val target = zipPath(job)
        if (target.exists() && target.length() > 0) return@withContext target
        Net.download(job.script, target, onProgress)
    }

    /** Copies a user-picked archive into app cache and returns a job that can be applied. */
    suspend fun stageLocal(ctx: Context, uri: android.net.Uri, label: String): Job? =
        withContext(Dispatchers.IO) {
            val name = label.ifBlank { "custom-skin" }
            val safe = name.replace(Regex("[^A-Za-z0-9 ._-]"), "_").take(48)
            val dest = File(zipDir, "custom_${System.currentTimeMillis()}_$safe.zip")
            try {
                ctx.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { out -> input.copyTo(out) }
                } ?: return@withContext null
            } catch (t: Throwable) {
                return@withContext null
            }
            if (!dest.exists() || dest.length() == 0L) return@withContext null
            Job(
                id = "custom-" + dest.nameWithoutExtension,
                kind = Job.Kind.SKIN,
                title = name,
                subtitle = "imported zip",
                script = "file://${dest.absolutePath}",
                image = null,
            )
        }

    suspend fun apply(
        storage: Storage,
        job: Job,
        onProgress: (Progress) -> Unit,
    ): Progress = withContext(Dispatchers.IO) {
        if (job.secondForm) {
            val active = prefs.currentSecondForm.value
            if (!active.isNullOrBlank() && active != job.id) {
                return@withContext Progress(
                    error = "Restore the active Second Form first",
                    finished = true,
                    phase = "error",
                    label = job.title,
                )
            }
            if (job.protectionRequired && !prefs.isApplied(Config.PROTECTION_JOB)) {
                return@withContext Progress(
                    error = "Enable Restoration Protection first",
                    finished = true,
                    phase = "error",
                    label = job.title,
                )
            }
        }
        onProgress(Progress(running = true, phase = "downloading", label = job.title))
        val zip = fetch(job) { bytes, total ->
            onProgress(
                Progress(
                    running = true,
                    phase = "downloading",
                    label = job.title,
                    bytes = bytes,
                    bytesTotal = total,
                )
            )
        } ?: return@withContext Progress(
            error = "download failed",
            finished = true,
            phase = "error",
            label = job.title,
        )
        onProgress(Progress(running = true, phase = "preparing", label = "reading archive"))
        val backupDir = File(backupRoot, job.id).apply { mkdirs() }

        val names = ArrayList<String>()
        var route: JSONObject? = null
        try {
            ZipInputStream(zip.inputStream().buffered()).use { zin ->
                while (true) {
                    val entry = zin.nextEntry ?: break
                    if (entry.isDirectory) {
                        zin.closeEntry()
                        continue
                    }
                    names.add(entry.name)
                    val base = entry.name.substringAfterLast('/')
                    if (base == "advanced_route.nxr" || base == ".nix-skill-atlas-patches.json") {
                        val payload = zin.readLimited(MAX_ENTRY)
                        if (payload != null) {
                            route = runCatching {
                                JSONObject(String(payload, Charsets.UTF_8))
                            }.getOrNull()
                        }
                    }
                    zin.closeEntry()
                }
            }
        } catch (t: Throwable) {
            return@withContext Progress(
                error = "archive is not readable", finished = true, phase = "error"
            )
        }

        val wrapper = wrapperFor(names)
        val rels = names.mapNotNull { normalize(it, wrapper) }
        val normalizedSet = rels.toHashSet()
        val aliases = atlasAliases(route).filterKeys { it in normalizedSet }
        val replacements = routeReplacements(route).filterKeys { it in normalizedSet }
        val extras = (aliases.values + replacements.values)
            .distinct()
            .filter { it !in normalizedSet }
        val total = rels.size + extras.size
        onProgress(
            Progress(
                running = true, done = 0, total = total,
                phase = "injecting", label = "writing files",
            )
        )

        val writtenTargets = HashSet<String>()
        var failures = 0

        fun writeRel(rel: String, bytes: ByteArray): Boolean {
            val backupFile = File(backupDir, rel)
            if (!backupFile.exists() && storage.exists(rel)) {
                val original = storage.read(rel)
                if (original != null && original.size <= MAX_ENTRY) {
                    backupFile.parentFile?.mkdirs()
                    backupFile.writeBytes(original)
                }
            }
            val wrote = storage.write(rel, bytes)
            val observed = storage.size(rel)
            val sizeOk = observed == null || observed < 0L ||
                observed == bytes.size.toLong() ||
                (observed == 0L && bytes.isNotEmpty())
            val observedHash = storage.hash(rel)
            val hashOk = observedHash == null || observedHash.equals(sha256(bytes), true)
            val ok = wrote && sizeOk && hashOk
            if (ok) {
                writtenTargets.add(rel)
            } else {
                failures++
            }
            onProgress(
                Progress(
                    running = true, done = writtenTargets.size, total = total,
                    phase = "injecting", label = rel.substringAfterLast('/'),
                )
            )
            return ok
        }

        try {
            ZipInputStream(zip.inputStream().buffered()).use { zin ->
                while (true) {
                    val entry = zin.nextEntry ?: break
                    if (entry.isDirectory) {
                        zin.closeEntry()
                        continue
                    }
                    val sourceRel = normalize(entry.name, wrapper)
                    if (sourceRel == null) {
                        zin.closeEntry()
                        continue
                    }
                    val bytes = zin.readLimited(MAX_ENTRY)
                    zin.closeEntry()
                    if (bytes == null) {
                        failures++
                        continue
                    }

                    val rel = mapRel(job, sourceRel)
                    writeRel(rel, bytes)
                    aliases[sourceRel]?.let { target -> writeRel(mapRel(job, target), bytes) }
                    replacements[sourceRel]?.let { target -> writeRel(target, bytes) }
                }
            }
        } catch (t: Throwable) {
            return@withContext Progress(
                error = t.message ?: "write failed", finished = true, phase = "error"
            )
        }

        val written = writtenTargets.size
        if (written == 0) {
            return@withContext Progress(
                error = "nothing written — check the folder permission",
                finished = true, phase = "error",
            )
        }
        if (failures > 0 && failures >= written) {
            return@withContext Progress(
                error = "writes rejected by the file provider",
                finished = true, phase = "error",
            )
        }

        when {
            job.id == Config.PROTECTION_OFF_JOB -> prefs.setSecondForm(null)
            job.secondForm -> prefs.setSecondForm(job.id)
        }
        prefs.record(
            AppliedRecord(
                jobId = job.id,
                title = job.title,
                subtitle = job.subtitle,
                kind = job.kind.name,
                files = written,
                at = System.currentTimeMillis(),
            )
        )
        Progress(
            running = false, done = written, total = written,
            finished = true, phase = "done", label = "complete",
        )
    }

    suspend fun restore(
        storage: Storage,
        jobId: String,
        onProgress: (Progress) -> Unit,
    ): Progress = withContext(Dispatchers.IO) {
        val dir = File(backupRoot, jobId)
        if (!dir.exists()) return@withContext Progress(
            error = "no backup for this item", finished = true, phase = "error"
        )
        val files = dir.walkTopDown().filter { it.isFile }.toList()
        if (files.isEmpty()) return@withContext Progress(
            error = "backup is empty", finished = true, phase = "error"
        )

        var done = 0
        var failed = 0
        for (file in files) {
            val rel = file.relativeTo(dir).path.replace(File.separatorChar, '/')
            val bytes = try {
                file.readBytes()
            } catch (t: Throwable) {
                null
            }
            if (bytes != null && storage.write(rel, bytes)) {
                done++
            } else {
                failed++
            }
            onProgress(
                Progress(
                    running = true, done = done, total = files.size,
                    phase = "restoring", label = rel.substringAfterLast('/'),
                )
            )
        }
        if (failed == 0) {
            dir.deleteRecursively()
            prefs.forget(jobId)
            if (prefs.currentSecondForm.value == jobId) prefs.setSecondForm(null)
            Progress(
                running = false, done = done, total = files.size,
                finished = true, phase = "done", label = "restored",
            )
        } else {
            Progress(error = "$failed files could not be restored", finished = true, phase = "error")
        }
    }

    suspend fun restoreAll(storage: Storage, onProgress: (Progress) -> Unit): Progress =
        withContext(Dispatchers.IO) {
            val jobs = backupRoot.listFiles()?.filter { it.isDirectory } ?: emptyList()
            if (jobs.isEmpty()) return@withContext Progress(
                error = "no backups stored", finished = true, phase = "error"
            )
            var done = 0
            var failed = 0
            var total = 0
            for (dir in jobs) {
                val files = dir.walkTopDown().filter { it.isFile }.toList()
                total += files.size
                for (file in files) {
                    val rel = file.relativeTo(dir).path.replace(File.separatorChar, '/')
                    val bytes = try {
                        file.readBytes()
                    } catch (t: Throwable) {
                        null
                    }
                    if (bytes != null && storage.write(rel, bytes)) {
                        done++
                        file.delete()
                    } else {
                        failed++
                    }
                }
                onProgress(
                    Progress(
                        running = true, done = done, total = total,
                        phase = "restoring", label = dir.name,
                    )
                )
                if (failed == 0) dir.deleteRecursively()
            }
            if (failed == 0) {
                prefs.clearApplied()
                prefs.setSecondForm(null)
                Progress(
                    running = false, done = done, total = done,
                    finished = true, phase = "done", label = "restored",
                )
            } else {
                Progress(error = "$failed files could not be restored", finished = true, phase = "error")
            }
        }

    fun backupSize(jobId: String): Long {
        val dir = File(backupRoot, jobId)
        if (!dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    fun backupCount(jobId: String): Int {
        val dir = File(backupRoot, jobId)
        if (!dir.exists()) return 0
        return dir.walkTopDown().count { it.isFile }
    }

    fun clearCache(): Int {
        val files = zipDir.listFiles() ?: return 0
        var freed = 0
        for (file in files) {
            if (file.delete()) freed++
        }
        return freed
    }

    companion object {
        private const val MAX_ENTRY = 96 * 1024 * 1024

        private fun sha256(bytes: ByteArray): String =
            MessageDigest.getInstance("SHA-256").digest(bytes)
                .joinToString("") { "%02x".format(it) }

        private val KNOWN_ROOTS = setOf(
            "art", "audio", "ui", "document", "astcinpack", "nix-advanced",
            "effects", "custom", "heroes", "splash", "background", "profile",
        )

        private fun stripKnown(path: String): String = when {
            path.startsWith("com.mobile.legends/files/dragon2017/assets/", true) ->
                path.substring("com.mobile.legends/files/dragon2017/assets/".length)
            path.startsWith("files/dragon2017/assets/", true) ->
                path.substring("files/dragon2017/assets/".length)
            path.startsWith("dragon2017/assets/", true) ->
                path.substring("dragon2017/assets/".length)
            path.startsWith("assets/", true) -> path.substring("assets/".length)
            else -> path
        }

        /** A single wrapping folder around every entry is a zip quirk, not a game path. */
        fun wrapperFor(names: List<String>): String? {
            val firsts = HashSet<String>()
            for (raw in names) {
                val path = stripKnown(raw.replace('\\', '/').trim().trimStart('/'))
                if (path.isBlank() || path.endsWith("/")) continue
                val slash = path.indexOf('/')
                if (slash <= 0) return null
                val first = path.substring(0, slash)
                if (first.contains('.')) return null
                firsts.add(first.lowercase())
            }
            if (firsts.size != 1) return null
            val only = firsts.first()
            if (only in KNOWN_ROOTS) return null
            return names.firstNotNullOfOrNull { raw ->
                val path = stripKnown(raw.replace('\\', '/').trim().trimStart('/'))
                val slash = path.indexOf('/')
                if (slash > 0 && path.substring(0, slash).lowercase() == only)
                    path.substring(0, slash + 1)
                else null
            }
        }

        fun normalize(raw: String, wrapper: String? = null): String? {
            var path = raw.replace('\\', '/').trim()
            path = path.trimStart('/')
            if (path.isBlank()) return null
            if (path.contains("__MACOSX")) return null
            if (path.endsWith("/")) return null
            path = stripKnown(path)
            if (wrapper != null && path.startsWith(wrapper, true)) {
                path = path.substring(wrapper.length)
            }
            if (path.isBlank()) return null
            val parts = path.split('/')
            if (parts.any { it == ".." || it == "." }) return null
            if (path.length > 240) return null
            return path
        }

        /** Intro archives ship a bare Splash.mp4; MLBB reads it from Audio/Android. */
        private fun mapRel(job: Job, rel: String): String {
            if (!job.intro) return rel
            val name = rel.substringAfterLast('/')
            return if (name.equals("Splash.mp4", true)) "Audio/Android/Splash.mp4" else rel
        }

        private fun atlasAliases(route: JSONObject?): Map<String, String> {
            if (route == null) return emptyMap()
            val arr = route.optJSONObject("skill_atlas")?.optJSONArray("atlases")
                ?: route.optJSONArray("atlases")
                ?: return emptyMap()
            val out = HashMap<String, String>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val alias = o.optString("alias_file").takeIf { it.isNotBlank() } ?: continue
                val target = o.optString("file").takeIf { it.isNotBlank() } ?: continue
                out[normalize(alias) ?: alias] = normalize(target) ?: target
            }
            return out
        }

        private fun routeReplacements(route: JSONObject?): Map<String, String> {
            if (route == null) return emptyMap()
            val arr = route.optJSONArray("replacements") ?: return emptyMap()
            val out = HashMap<String, String>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val payload = o.optString("payload_ref").takeIf { it.isNotBlank() } ?: continue
                val entry = o.optString("entry").takeIf { it.isNotBlank() } ?: continue
                out[normalize(payload) ?: payload] =
                    normalize("Document/android/$entry") ?: "Document/android/$entry"
            }
            return out
        }
    }
}

private fun ZipInputStream.readLimited(limit: Int): ByteArray? {
    val out = java.io.ByteArrayOutputStream()
    val buf = ByteArray(64 * 1024)
    var total = 0
    while (true) {
        val n = read(buf)
        if (n <= 0) break
        total += n
        if (total > limit) return null
        out.write(buf, 0, n)
    }
    return out.toByteArray()
}
