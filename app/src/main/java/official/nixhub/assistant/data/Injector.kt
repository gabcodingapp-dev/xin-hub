package official.nixhub.assistant.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
    )

    private fun zipPath(job: Job): File {
        val name = job.script.substringAfterLast('/').substringBefore('?').ifBlank { "script.zip" }
        val hash = job.script.hashCode().toUInt().toString(16)
        return File(zipDir, "${hash}_${name.takeLast(48)}")
    }

    suspend fun fetch(job: Job, onBytes: (Long) -> Unit = {}): File? = withContext(Dispatchers.IO) {
        if (job.script.startsWith("file:")) {
            val path = job.script.removePrefix("file://")
            val local = File(path)
            return@withContext if (local.exists() && local.length() > 0) local else null
        }
        val target = zipPath(job)
        if (target.exists() && target.length() > 0) return@withContext target
        Net.download(job.script, target, onBytes)
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
        val zip = fetch(job) ?: return@withContext Progress(
            error = "download failed", finished = true
        )
        val backupDir = File(backupRoot, job.id).apply { mkdirs() }

        var written = 0
        var total = 0
        try {
            ZipInputStream(zip.inputStream().buffered()).use { zin ->
                while (true) {
                    val entry = zin.nextEntry ?: break
                    if (!entry.isDirectory) total++
                    zin.closeEntry()
                }
            }
        } catch (t: Throwable) {
            return@withContext Progress(error = "archive is not readable", finished = true)
        }
        onProgress(Progress(running = true, done = 0, total = total, label = "applying"))

        var failures = 0
        try {
            ZipInputStream(zip.inputStream().buffered()).use { zin ->
                while (true) {
                    val entry = zin.nextEntry ?: break
                    if (entry.isDirectory) {
                        zin.closeEntry()
                        continue
                    }
                    val rel = normalize(entry.name)
                    if (rel == null) {
                        zin.closeEntry()
                        continue
                    }
                    val bytes = zin.readLimited(MAX_ENTRY)
                    zin.closeEntry()
                    if (bytes == null) {
                        failures++
                        continue
                    }

                    val backupFile = File(backupDir, rel)
                    if (!backupFile.exists() && storage.exists(rel)) {
                        val original = storage.read(rel)
                        if (original != null && original.size <= MAX_ENTRY) {
                            backupFile.parentFile?.mkdirs()
                            backupFile.writeBytes(original)
                        }
                    }
                    if (storage.write(rel, bytes)) {
                        written++
                    } else {
                        failures++
                    }
                    onProgress(Progress(running = true, done = written, total = total, label = rel.substringAfterLast('/')))
                }
            }
        } catch (t: Throwable) {
            return@withContext Progress(error = t.message ?: "write failed", finished = true)
        }

        if (written == 0) {
            return@withContext Progress(error = "nothing written — check the folder permission", finished = true)
        }
        if (failures > 0 && failures >= written) {
            return@withContext Progress(error = "writes rejected by the file provider", finished = true)
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
        Progress(running = false, done = written, total = written, finished = true)
    }

    suspend fun restore(
        storage: Storage,
        jobId: String,
        onProgress: (Progress) -> Unit,
    ): Progress = withContext(Dispatchers.IO) {
        val dir = File(backupRoot, jobId)
        if (!dir.exists()) return@withContext Progress(error = "no backup for this item", finished = true)
        val files = dir.walkTopDown().filter { it.isFile }.toList()
        if (files.isEmpty()) return@withContext Progress(error = "backup is empty", finished = true)

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
            onProgress(Progress(running = true, done = done, total = files.size, label = rel.substringAfterLast('/')))
        }
        if (failed == 0) {
            dir.deleteRecursively()
            prefs.forget(jobId)
            Progress(running = false, done = done, total = files.size, finished = true)
        } else {
            Progress(error = "$failed files could not be restored", finished = true)
        }
    }

    suspend fun restoreAll(storage: Storage, onProgress: (Progress) -> Unit): Progress =
        withContext(Dispatchers.IO) {
            val jobs = backupRoot.listFiles()?.filter { it.isDirectory } ?: emptyList()
            if (jobs.isEmpty()) return@withContext Progress(error = "no backups stored", finished = true)
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
                onProgress(Progress(running = true, done = done, total = total, label = dir.name))
                if (failed == 0) dir.deleteRecursively()
            }
            if (failed == 0) {
                prefs.clearApplied()
                Progress(running = false, done = done, total = done, finished = true)
            } else {
                Progress(error = "$failed files could not be restored", finished = true)
            }
        }

    fun backupSize(jobId: String): Long {        val dir = File(backupRoot, jobId)
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

        fun normalize(raw: String): String? {
            var path = raw.replace('\\', '/').trim()
            path = path.trimStart('/')
            if (path.isBlank()) return null
            if (path.contains("__MACOSX")) return null
            if (path.endsWith("/")) return null
            path = when {
                path.startsWith("com.mobile.legends/files/dragon2017/assets/") ->
                    path.removePrefix("com.mobile.legends/files/dragon2017/assets/")
                path.startsWith("dragon2017/assets/") -> path.removePrefix("dragon2017/assets/")
                path.startsWith("assets/") -> path.removePrefix("assets/")
                path.startsWith("files/dragon2017/assets/") -> path.removePrefix("files/dragon2017/assets/")
                else -> path
            }
            if (path.isBlank()) return null
            val parts = path.split('/')
            if (parts.any { it == ".." || it == "." }) return null
            if (path.length > 240) return null
            return path
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
