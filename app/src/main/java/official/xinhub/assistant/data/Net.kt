package official.xinhub.assistant.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object Net {
    private const val UA = "XinHub/1.0 (Android)"

    suspend fun get(url: String, timeoutMs: Int = 20_000): ByteArray? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", UA)
                setRequestProperty("Accept-Encoding", "identity")
            }
            if (conn.responseCode !in 200..299) return@withContext null
            conn.inputStream.use { it.readBytes() }
        } catch (t: Throwable) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    /** Streams a zip to disk. Reports bytes written and the server-declared total. */
    suspend fun download(
        url: String,
        dest: File,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): File? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 60_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", UA)
            }
            if (conn.responseCode !in 200..299) return@withContext null
            val total = runCatching { conn.contentLengthLong }.getOrDefault(-1L).takeIf { it > 0 } ?: 0L
            dest.parentFile?.mkdirs()
            val tmp = File(dest.parentFile, dest.name + ".part")
            var written = 0L
            conn.inputStream.use { input ->
                tmp.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        output.write(buf, 0, n)
                        written += n
                        onProgress(written, total)
                    }
                    output.flush()
                }
            }
            if (written <= 0L) {
                tmp.delete()
                null
            } else if (!tmp.renameTo(dest)) {
                dest.delete()
                tmp.renameTo(dest)
                dest.takeIf { it.exists() }
            } else {
                dest
            }
        } catch (t: Throwable) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
