package official.xinhub.assistant.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/** Catalog loading: bundled snapshot first, remote refresh on top. */
class CatalogStore(private val ctx: Context) {

    @Volatile
    var current: Catalog = Catalog.EMPTY
        private set

    var version: String = ""
        private set

    private var mirrorBase: String = ""
    private var mirrorItems: Map<String, String> = emptyMap()

    /** Mirrored release asset for a catalog id, if the mirror already has it. */
    fun mirrorUrl(id: String): String? {
        val name = mirrorItems[id] ?: return null
        if (mirrorBase.isBlank()) return null
        return "$mirrorBase/$name"
    }

    private fun loadMirror(text: String?) {
        if (text.isNullOrBlank()) return
        try {
            val root = JSONObject(text)
            val base = root.optString("base")
            val items = root.optJSONObject("items") ?: return
            val map = HashMap<String, String>(items.length())
            val keys = items.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val entry = items.optJSONObject(key) ?: continue
                val tag = entry.optString("tag")
                val name = entry.optString("name")
                if (tag.isNotBlank() && name.isNotBlank()) map[key] = "$tag/$name"
            }
            mirrorBase = base
            mirrorItems = map
        } catch (t: Throwable) {
            // A broken mirror index must never take the catalog down.
        }
    }

    private val cacheDir: File get() = File(ctx.filesDir, "catalog").apply { mkdirs() }

    private fun readBundled(name: String): String? = try {
        ctx.assets.open("catalog/$name").bufferedReader().use { it.readText() }
    } catch (t: Throwable) {
        null
    }

    private fun readCache(name: String): String? = try {
        File(cacheDir, name).takeIf { it.exists() }?.readText()
    } catch (t: Throwable) {
        null
    }

    private fun parse(read: (String) -> String?): Catalog? {
        val skins = read("skins.json") ?: return null
        val effects = read("effects.json") ?: return null
        val heroes = read("heroes.json")
        val tags = read("tags.json")
        val menus = read("menus.json")
        val lobby = read("lobby_sounds.json")
        val latest = read("latest.json")
        val upgrades = read("upgrade.json") ?: readBundled("upgrade.json")
        return Catalog(
            version = "",
            skins = Json.skins(JSONObject(skins)),
            heroes = heroes?.let { Json.heroes(JSONObject(it)) } ?: emptyList(),
            effects = Json.effects(JSONObject(effects)),
            lobby = lobby?.let { Json.lobby(JSONObject(it)) } ?: emptyList(),
            menus = menus?.let { Json.menus(JSONObject(it)) } ?: emptyList(),
            tags = tags?.let { Json.tags(JSONObject(it)) } ?: emptyList(),
            latest = latest?.let { Json.latest(JSONObject(it)) } ?: emptyList(),
            upgrades = upgrades?.let { Json.upgrades(JSONObject(it)) } ?: emptyList(),
        )
    }

    suspend fun load(): LoadState = withContext(Dispatchers.IO) {
        loadMirror(readCache("mirror.json"))
        val cached = parse { readCache(it) }
        val catalog = cached ?: parse { readBundled(it) }
        val source = if (cached != null) "cache" else "bundled"
        if (catalog == null) {
            LoadState.Failed("catalog missing")
        } else {
            current = catalog
            LoadState.Ready(catalog, source)
        }
    }

    /** Returns true when a newer catalog landed. */
    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        val manifestBytes = Net.get(Config.MANIFEST_URL) ?: return@withContext false
        val manifest = JSONObject(manifestBytes.toString(Charsets.UTF_8))
        val remoteVersion = manifest.optString("catalog_version")
        val endpoints = manifest.optJSONObject("endpoints") ?: return@withContext false
        if (remoteVersion.isBlank()) return@withContext false
        if (remoteVersion == version) return@withContext false

        val names = listOf("skins", "effects", "heroes", "tags", "menus", "lobby", "latest", "upgrade")
        val staged = HashMap<String, ByteArray>()
        for (key in names) {
            val rel = endpoints.optString(key)
            if (rel.isBlank()) continue
            val url = if (rel.startsWith("http")) rel else Config.ASSETS_BASE + rel
            val bytes = Net.get(url) ?: continue
            staged[key] = bytes
        }
        Net.get(Config.ASSETS_BASE + "catalog/mirror.json")?.let { staged["mirror"] = it }
        if (staged["skins"] == null || staged["effects"] == null) return@withContext false

        for ((key, bytes) in staged) {
            val file = File(cacheDir, "$key.json")
            val tmp = File(cacheDir, "$key.json.tmp")
            tmp.writeBytes(bytes)
            if (!tmp.renameTo(file)) {
                file.writeBytes(bytes)
                tmp.delete()
            }
        }
        File(cacheDir, "version").writeText(remoteVersion)

        loadMirror(readCache("mirror.json"))
        val fresh = parse { readCache(it) } ?: return@withContext false
        current = fresh
        version = remoteVersion
        true
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
