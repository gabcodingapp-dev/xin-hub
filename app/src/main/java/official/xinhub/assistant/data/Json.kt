package official.xinhub.assistant.data

import org.json.JSONArray
import org.json.JSONObject

/** Hand-rolled parsers. org.json ships with Android, no serialization plugin needed. */
object Json {

    private fun JSONArray.strings(): List<String> {
        val out = ArrayList<String>(length())
        for (i in 0 until length()) out.add(optString(i))
        return out
    }

    private fun JSONArray.objects(): List<JSONObject> {
        val out = ArrayList<JSONObject>(length())
        for (i in 0 until length()) optJSONObject(i)?.let { out.add(it) }
        return out
    }

    private fun JSONObject.arr(name: String): JSONArray = optJSONArray(name) ?: JSONArray()

    private fun JSONObject.text(name: String): String? =
        optString(name).takeIf { it.isNotBlank() && it != "null" }

    private fun advanced(o: JSONObject?): AdvancedCompatibility? {
        if (o == null) return null
        val type = o.text("type") ?: return null
        return AdvancedCompatibility(
            type = type,
            embedded = o.optBoolean("embedded", false),
            routeFile = o.text("route_file"),
            routeFormat = o.text("route_format"),
            minimumEngine = o.optInt("minimum_engine", 0),
            restorationProtectionRequired = o.optBoolean("restoration_protection_required", false),
        )
    }

    fun skins(root: JSONObject): List<Skin> = root.arr("data").objects().map { o ->
        Skin(
            id = o.optString("id"),
            hero = o.optString("hero"),
            heroId = o.optInt("hero_id"),
            heroSlug = o.optString("hero_slug"),
            name = o.optString("name"),
            tier = o.optString("tier"),
            tag = o.text("tag"),
            source = o.optString("source"),
            image = o.text("image"),
            script = o.text("script"),
            archive = o.text("archive"),
            upgrade = o.optBoolean("upgrade", false),
            splash = o.text("splash"),
            scriptRelease = o.text("script_release"),
            scriptEndpoint = o.text("script_endpoint"),
            catalog = o.text("catalog"),
            archiveRevision = o.text("archive_revision"),
            advanced = advanced(o.optJSONObject("advanced_compatibility")),
        )
    }

    fun upgrades(root: JSONObject): List<UpgradeEntry> = root.arr("data").objects().map { o ->
        UpgradeEntry(
            id = o.optString("id"),
            hero = o.optString("hero"),
            heroSlug = o.optString("hero_slug"),
            name = o.optString("name"),
            tier = o.optString("tier"),
            tag = o.text("tag"),
            image = o.text("image"),
            portrait = o.text("portrait"),
            script = o.text("script"),
            scriptRelease = o.text("script_release"),
            catalog = o.text("catalog"),
            archiveRevision = o.text("archive_revision"),
            secondForm = o.optBoolean("second_form", false),
            protectionRequired = o.optBoolean("protection_required", false),
            routeFile = o.text("route_file"),
            routeFormat = o.text("route_format"),
            enabled = o.optBoolean("enabled", true),
        )
    }

    fun heroes(root: JSONObject): List<Hero> = root.arr("data").objects().map { o ->
        Hero(
            slug = o.optString("slug"),
            name = o.optString("name"),
            heroId = o.optInt("hero_id"),
            roles = o.arr("roles").strings(),
            head = o.optString("head").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    fun effects(root: JSONObject): List<EffectCategory> = root.arr("data").objects().map { c ->
        EffectCategory(
            id = c.optString("id"),
            title = c.optString("title"),
            description = c.optString("description"),
            kind = c.optString("kind", "effect"),
            items = c.arr("items").objects().map { o ->
                EffectItem(
                    id = o.optString("id"),
                    name = o.optString("name"),
                    image = o.optString("image").takeIf { it.isNotBlank() && it != "null" },
                    script = o.optString("script"),
                    size = o.optLong("size"),
                    preview = o.text("preview"),
                    views = o.optInt("views", 0),
                )
            },
        )
    }

    fun lobby(root: JSONObject): List<LobbySound> = root.arr("data").objects().map { o ->
        LobbySound(
            title = o.optString("title"),
            artist = o.optString("artist"),
            image = o.optString("image").takeIf { it.isNotBlank() && it != "null" },
            download = o.optString("download"),
            preview = o.optString("preview").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    fun menus(root: JSONObject): List<MenuEntry> = root.arr("data").objects().map { o ->
        MenuEntry(
            id = o.optString("id"),
            title = o.optString("title"),
            description = o.optString("description"),
            image = o.optString("image").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    fun tags(root: JSONObject): List<Tag> = root.arr("data").objects().map { o ->
        Tag(
            id = o.optString("id"),
            name = o.optString("name"),
            image = o.optString("image").takeIf { it.isNotBlank() && it != "null" },
        )
    }

    fun latest(root: JSONObject): List<LatestSkin> = root.arr("data").objects().map { o ->
        LatestSkin(
            id = o.optString("id"),
            hero = o.optString("hero"),
            name = o.optString("name"),
            tier = o.optString("tier"),
            tag = o.text("tag"),
            image = o.text("image"),
            heroId = o.optInt("hero_id", 0),
            splashEndpoint = o.text("splash_endpoint"),
            scriptEndpoint = o.text("script_endpoint"),
            advanced = advanced(o.optJSONObject("advanced_compatibility")),
        )
    }
}
