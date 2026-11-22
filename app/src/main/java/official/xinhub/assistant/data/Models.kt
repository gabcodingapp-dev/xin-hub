package official.xinhub.assistant.data

data class Skin(
    val id: String,
    val hero: String,
    val heroId: Int,
    val heroSlug: String,
    val name: String,
    val tier: String,
    val tag: String?,
    val source: String,
    val image: String?,
    val script: String?,
    val archive: String?,
    val upgrade: Boolean = false,
    val splash: String? = null,
    val scriptRelease: String? = null,
    val scriptEndpoint: String? = null,
    val catalog: String? = null,
    val archiveRevision: String? = null,
    val advanced: AdvancedCompatibility? = null,
) {
    val injectable: Boolean get() = !bestScript.isNullOrBlank()
    val bestScript: String? get() = scriptRelease?.takeIf { it.isNotBlank() } ?: script
    val secondForm: Boolean get() = advanced?.type == "second_form"
    val group: String get() = when {
        source == "custom" -> "custom"
        upgrade || source == "painted" -> "upgrade"
        else -> "visual"
    }
}

data class AdvancedCompatibility(
    val type: String,
    val embedded: Boolean = false,
    val routeFile: String? = null,
    val routeFormat: String? = null,
    val minimumEngine: Int = 0,
    val restorationProtectionRequired: Boolean = false,
)

data class UpgradeEntry(
    val id: String,
    val hero: String,
    val heroSlug: String,
    val name: String,
    val tier: String,
    val tag: String?,
    val image: String?,
    val portrait: String?,
    val script: String?,
    val scriptRelease: String? = null,
    val catalog: String? = null,
    val archiveRevision: String? = null,
    val secondForm: Boolean = false,
    val protectionRequired: Boolean = false,
    val routeFile: String? = null,
    val routeFormat: String? = null,
    val enabled: Boolean = true,
) {
    val bestScript: String? get() = scriptRelease?.takeIf { it.isNotBlank() } ?: script
    val injectable: Boolean get() = !bestScript.isNullOrBlank()
}

data class Hero(
    val slug: String,
    val name: String,
    val heroId: Int,
    val roles: List<String>,
    val head: String?,
)

data class EffectItem(
    val id: String,
    val name: String,
    val image: String?,
    val script: String,
    val size: Long,
    val preview: String? = null,
    val views: Int = 0,
)

data class EffectCategory(
    val id: String,
    val title: String,
    val description: String,
    val kind: String,
    val items: List<EffectItem>,
)

data class LobbySound(
    val title: String,
    val artist: String,
    val image: String?,
    val download: String,
    val preview: String?,
)

data class MenuEntry(
    val id: String,
    val title: String,
    val description: String,
    val image: String?,
)

data class Tag(
    val id: String,
    val name: String,
    val image: String?,
)

data class LatestSkin(
    val id: String,
    val hero: String,
    val name: String,
    val tier: String,
    val tag: String?,
    val image: String?,
    val heroId: Int = 0,
    val splashEndpoint: String? = null,
    val scriptEndpoint: String? = null,
    val advanced: AdvancedCompatibility? = null,
)

data class Catalog(
    val version: String,
    val skins: List<Skin>,
    val heroes: List<Hero>,
    val effects: List<EffectCategory>,
    val lobby: List<LobbySound>,
    val menus: List<MenuEntry>,
    val tags: List<Tag>,
    val latest: List<LatestSkin>,
    val upgrades: List<UpgradeEntry> = emptyList(),
) {
    val injectableCount: Int get() = skins.count { it.injectable }
    val effectCount: Int get() = effects.sumOf { it.items.size }
    val secondFormCount: Int get() = upgrades.count { it.secondForm && it.enabled }

    companion object {
        val EMPTY = Catalog("", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
}

/** One thing the user asked the injector to put into the game. */
data class Job(
    val id: String,
    val kind: Kind,
    val title: String,
    val subtitle: String,
    val script: String,
    val image: String?,
    val slotId: String? = null,
    val slotName: String? = null,
    val upgradeMode: Boolean = false,
    val secondForm: Boolean = false,
    val protectionRequired: Boolean = false,
    val intro: Boolean = false,
) {
    enum class Kind { SKIN, EFFECT, LOBBY }
}

data class AppliedRecord(
    val jobId: String,
    val title: String,
    val subtitle: String,
    val kind: String,
    val files: Int,
    val at: Long,
)

sealed interface LoadState {
    data object Idle : LoadState
    data object Loading : LoadState
    data class Ready(val catalog: Catalog, val source: String) : LoadState
    data class Failed(val message: String) : LoadState
}
