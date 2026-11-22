package official.nixhub.assistant.data

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
) {
    val injectable: Boolean get() = !script.isNullOrBlank()
    val group: String get() = when {
        source == "custom" -> "custom"
        upgrade || source == "painted" -> "upgrade"
        else -> "visual"
    }
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
) {
    val injectableCount: Int get() = skins.count { it.injectable }
    val effectCount: Int get() = effects.sumOf { it.items.size }

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
