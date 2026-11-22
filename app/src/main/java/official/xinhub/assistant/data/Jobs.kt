package official.xinhub.assistant.data

fun Skin.toJob(): Job? {
    val link = bestScript ?: return null
    return Job(
        id = id,
        kind = Job.Kind.SKIN,
        title = name,
        subtitle = "$hero · ${tier.ifBlank { "skin" }}",
        script = link,
        image = splash ?: image,
        secondForm = secondForm,
        protectionRequired = advanced?.restorationProtectionRequired ?: false,
    )
}

fun UpgradeEntry.toJob(slot: Skin? = null): Job? {
    val link = bestScript ?: return null
    val idPart = slot?.id ?: "direct"
    return Job(
        id = "upgrade-$id-$idPart",
        kind = Job.Kind.SKIN,
        title = name,
        subtitle = if (slot == null) "$hero · upgrade" else "$hero · over ${slot.name}",
        script = link,
        image = image ?: portrait,
        slotId = slot?.id,
        slotName = slot?.name,
        upgradeMode = true,
        secondForm = secondForm,
        protectionRequired = protectionRequired,
    )
}

fun EffectItem.toJob(category: EffectCategory): Job? {
    val link = script.takeIf { it.isNotBlank() && it != "null" } ?: return null
    return Job(
        id = id,
        kind = Job.Kind.EFFECT,
        title = name,
        subtitle = category.title,
        script = link,
        image = image,
    )
}

fun LobbySound.toJob(): Job? {
    val link = download.takeIf { it.isNotBlank() } ?: return null
    return Job(
        id = "lobby-" + title.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'),
        kind = Job.Kind.LOBBY,
        title = title,
        subtitle = artist.ifBlank { "lobby sound" },
        script = link,
        image = image,
    )
}

fun LatestSkin.toJob(skins: List<Skin>): Job? {
    val match = skins.firstOrNull { it.id == id } ?: return null
    return match.toJob()
}
