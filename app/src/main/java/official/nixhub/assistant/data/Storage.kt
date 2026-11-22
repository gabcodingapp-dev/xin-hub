package official.nixhub.assistant.data

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import rikka.shizuku.Shizuku

/**
 * Where the game files live. Two backends: SAF tree picked by the user, or a root shell.
 * Relative paths are always relative to `dragon2017/assets`.
 */
interface Storage {
    val label: String
    val writable: Boolean
    fun exists(rel: String): Boolean
    fun read(rel: String): ByteArray?
    fun write(rel: String, bytes: ByteArray): Boolean
    fun walk(): List<String>
}

private fun safeRel(rel: String): String = rel.trim('/')

class SafStorage(
    private val context: Context,
    private val assetsRoot: DocumentFile,
) : Storage {
    override val label: String get() = "SAF · ${assetsRoot.name ?: "mlbb"}"
    override val writable: Boolean get() = assetsRoot.canWrite()

    private val dirCache = HashMap<String, DocumentFile?>()

    private fun dir(relDir: String, create: Boolean): DocumentFile? {
        if (relDir.isEmpty()) return assetsRoot
        dirCache[relDir]?.let { return it }
        var cur = assetsRoot
        for (part in relDir.split('/')) {
            val next = cur?.findFile(part)?.takeIf { it.isDirectory }
                ?: if (create) cur?.createDirectory(part) else null
            cur = next ?: return null
        }
        if (cur != null) dirCache[relDir] = cur
        return cur
    }

    private fun locate(rel: String): DocumentFile? {
        val path = safeRel(rel)
        val slash = path.lastIndexOf('/')
        val parent = if (slash < 0) "" else path.substring(0, slash)
        val name = if (slash < 0) path else path.substring(slash + 1)
        return dir(parent, false)?.findFile(name)
    }

    override fun exists(rel: String): Boolean = locate(rel)?.exists() == true

    override fun read(rel: String): ByteArray? = try {
        locate(rel)?.let { doc ->
            context.contentResolver.openInputStream(doc.uri)?.use { it.readBytes() }
        }
    } catch (t: Throwable) {
        null
    }

    override fun write(rel: String, bytes: ByteArray): Boolean = try {
        val path = safeRel(rel)
        val slash = path.lastIndexOf('/')
        val parent = if (slash < 0) "" else path.substring(0, slash)
        val name = if (slash < 0) path else path.substring(slash + 1)
        val parentDir = dir(parent, true) ?: return false
        val existing = parentDir.findFile(name)
        val doc = existing?.takeIf { it.isFile }
            ?: parentDir.createFile("application/octet-stream", name)
        if (doc == null) {
            false
        } else {
            context.contentResolver.openOutputStream(doc.uri, "wt")?.use { out ->
                out.write(bytes)
                out.flush()
            } != null
        }
    } catch (t: Throwable) {
        false
    }

    override fun walk(): List<String> {
        val out = ArrayList<String>()
        fun rec(node: DocumentFile, prefix: String, depth: Int) {
            if (depth > 12) return
            for (child in node.listFiles()) {
                val name = child.name ?: continue
                val path = if (prefix.isEmpty()) name else "$prefix/$name"
                if (child.isDirectory) rec(child, path, depth + 1) else out.add(path)
            }
        }
        rec(assetsRoot, "", 0)
        return out
    }

    companion object {
        /** Resolve a picked tree into `files/dragon2017/assets`, accepting either `files` or `assets`. */
        fun fromUri(context: Context, uri: Uri): SafStorage? {
            val root = DocumentFile.fromTreeUri(context, uri) ?: return null
            val assets = root.findFile("dragon2017")?.findFile("assets")
                ?: root.takeIf { root.name == "assets" }
                ?: return null
            return SafStorage(context, assets)
        }
    }
}

class RootStorage(private val base: String) : Storage {    override val label: String get() = "root · $base"
    override val writable: Boolean get() = true

    private fun sh(cmd: String, input: ByteArray? = null): Pair<Int, ByteArray> {
        val p = ProcessBuilder("su", "-c", cmd).redirectErrorStream(false).start()
        if (input != null) {
            p.outputStream.use { it.write(input) }
        } else {
            p.outputStream.close()
        }
        val out = p.inputStream.use { it.readBytes() }
        val code = p.waitFor()
        return code to out
    }

    private fun q(path: String): String = "'" + path.replace("'", "'\\''") + "'"

    private fun full(rel: String): String = "$base/${safeRel(rel)}"

    override fun exists(rel: String): Boolean {
        val (code, out) = sh("test -e ${q(full(rel))} && echo 1 || echo 0")
        return code == 0 && out.toString(Charsets.UTF_8).trim() == "1"
    }

    override fun read(rel: String): ByteArray? {
        val (code, out) = sh("cat ${q(full(rel))} 2>/dev/null")
        return if (code == 0 && out.isNotEmpty()) out else null
    }

    override fun write(rel: String, bytes: ByteArray): Boolean {
        val target = full(rel)
        val parent = target.substringBeforeLast('/')
        val (code, _) = sh("mkdir -p ${q(parent)} && cat > ${q(target)}", bytes)
        return code == 0
    }

    override fun walk(): List<String> {
        val (code, out) = sh("find ${q(base)} -type f 2>/dev/null")
        if (code != 0) return emptyList()
        val prefix = "$base/"
        return out.toString(Charsets.UTF_8).lineSequence()
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix) }
            .toList()
    }

    companion object {
        const val MLBB_FILES = "/storage/emulated/0/Android/data/com.mobile.legends/files"
        val ASSETS = "$MLBB_FILES/dragon2017/assets"

        fun available(): Boolean = try {
            val p = ProcessBuilder("su", "-c", "id").start()
            p.waitFor() == 0
        } catch (t: Throwable) {
            false
        }
    }
}

/**
 * Shell access through Shizuku. Same command surface as root, but the process runs as the
 * adb shell uid, so no superuser prompt and no boot loop risk.
 */
class ShizukuStorage(private val base: String) : Storage {
    override val label: String get() = "shizuku · $base"
    override val writable: Boolean get() = ShizukuAccess.granted()

    private fun sh(cmd: String, input: ByteArray? = null): Pair<Int, ByteArray> {
        val p = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null)
        if (input != null) p.outputStream.use { it.write(input) } else p.outputStream.close()
        val out = p.inputStream.use { it.readBytes() }
        val code = p.waitFor()
        return code to out
    }

    private fun q(path: String): String = "'" + path.replace("'", "'\\''") + "'"

    private fun full(rel: String): String = "$base/${safeRel(rel)}"

    override fun exists(rel: String): Boolean {
        val (code, out) = sh("test -e ${q(full(rel))} && echo 1 || echo 0")
        return code == 0 && out.toString(Charsets.UTF_8).trim() == "1"
    }

    override fun read(rel: String): ByteArray? {
        val (code, out) = sh("cat ${q(full(rel))} 2>/dev/null")
        return if (code == 0 && out.isNotEmpty()) out else null
    }

    override fun write(rel: String, bytes: ByteArray): Boolean {
        val target = full(rel)
        val parent = target.substringBeforeLast('/')
        val (code, _) = sh("mkdir -p ${q(parent)} && cat > ${q(target)}", bytes)
        return code == 0
    }

    override fun walk(): List<String> {
        val (code, out) = sh("find ${q(base)} -type f 2>/dev/null")
        if (code != 0) return emptyList()
        val prefix = "$base/"
        return out.toString(Charsets.UTF_8).lineSequence()
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix) }
            .toList()
    }
}

data class ShizukuStatus(
    val installed: Boolean,
    val running: Boolean,
    val granted: Boolean,
)

object ShizukuAccess {
    const val REQUEST_CODE = 4242

    fun installed(ctx: Context): Boolean = try {
        ctx.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
        true
    } catch (t: Throwable) {
        false
    }

    fun status(ctx: Context): ShizukuStatus {
        val running = running()
        return ShizukuStatus(
            installed = installed(ctx),
            running = running,
            granted = running && granted(),
        )
    }

    fun running(): Boolean = try {
        Shizuku.pingBinder()
    } catch (t: Throwable) {
        false
    }

    fun granted(): Boolean = try {
        running() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (t: Throwable) {
        false
    }

    fun request(): Boolean = try {
        if (!running()) {
            false
        } else if (granted()) {
            true
        } else {
            Shizuku.requestPermission(REQUEST_CODE)
            false
        }
    } catch (t: Throwable) {
        false
    }

    fun assets(): Storage = ShizukuStorage(RootStorage.ASSETS)
}
