package official.xinhub.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.Config
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.data.Skin
import official.xinhub.assistant.data.UpgradeEntry
import official.xinhub.assistant.data.toJob
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.EmptyState
import official.xinhub.assistant.ui.components.GhostAction
import official.xinhub.assistant.ui.components.GlassCard
import official.xinhub.assistant.ui.components.MenuThumb
import official.xinhub.assistant.ui.components.Pill
import official.xinhub.assistant.ui.components.PrimaryAction
import official.xinhub.assistant.ui.components.SearchField
import official.xinhub.assistant.ui.components.ScreenHeader
import official.xinhub.assistant.ui.components.SectionTitle
import official.xinhub.assistant.ui.components.SegmentedTabs
import official.xinhub.assistant.ui.components.SheetHandle
import official.xinhub.assistant.ui.components.pressable
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Danger
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.XinDisplay
import official.xinhub.assistant.ui.theme.tierColor

private fun protectionJob(): Job = Job(
    id = Config.PROTECTION_JOB,
    kind = Job.Kind.EFFECT,
    title = "Restoration Protection",
    subtitle = "Second Form safeguard",
    script = Config.PROTECT_ON,
    image = null,
)

@Composable
fun UpgradeScreen(hub: HubState, onBack: () -> Unit, onApply: (Job) -> Unit) {
    val upgrades = remember(hub.catalog.upgrades) {
        hub.catalog.upgrades
            .filter { it.enabled }
            .sortedWith(compareBy({ !it.secondForm }, { it.hero }, { it.name }))
    }
    val secondForms = remember(upgrades) { upgrades.filter { it.secondForm } }
    val readyCount = remember(upgrades) { upgrades.count { it.injectable } }
    var filter by rememberSaveable { mutableStateOf("all") }
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<UpgradeEntry?>(null) }

    val shown by remember(upgrades, filter, query) {
        derivedStateOf {
            upgrades.filter { entry ->
                val matchFilter = when (filter) {
                    "second" -> entry.secondForm
                    "regular" -> !entry.secondForm
                    else -> true
                }
                val matchQuery = query.isBlank() ||
                    entry.name.contains(query, true) ||
                    entry.hero.contains(query, true) ||
                    entry.tag.orEmpty().contains(query, true)
                matchFilter && matchQuery
            }
        }
    }
    val featured = secondForms.firstOrNull() ?: upgrades.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "UPGRADE SKINS",
            subtitle = "${upgrades.size} looks · $readyCount ready",
            onBack = onBack,
        ) {
            Pill("FREE", Cyan, filled = true)
        }

        Spacer(Modifier.height(8.dp))
        ProtocolStrip(onApply = onApply)

        Spacer(Modifier.height(9.dp))
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            SearchField(query, "Search upgrades") { query = it }
        }
        Spacer(Modifier.height(7.dp))
        SegmentedTabs(
            items = listOf(
                Triple("all", "All", upgrades.size),
                Triple("second", "Second Form", secondForms.size),
                Triple("regular", "Regular", upgrades.size - secondForms.size),
            ),
            selected = filter,
            onSelect = { filter = it },
        )

        Spacer(Modifier.height(6.dp))
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().weight(1f),
        ) {
            if (filter != "second" && featured != null) {
                item(key = "featured-${featured.id}") {
                    FeaturedUpgrade(featured) { selected = featured }
                }
            }

            if (filter != "regular" && secondForms.isNotEmpty()) {
                item(key = "second-title") {
                    SectionTitle("Second Forms", secondForms.size, "FREE")
                }
                item(key = "second-row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        items(secondForms, key = { it.id }) { entry ->
                            SecondFormTile(entry) { selected = entry }
                        }
                    }
                }
            }

            item(key = "all-title") {
                SectionTitle("All upgrades", shown.size)
            }
            if (shown.isEmpty()) {
                item(key = "empty") {
                    EmptyState("No upgrade skins", "Try another filter or clear the search.")
                }
            } else {
                items(shown, key = { it.id }) { entry ->
                    UpgradeRow(entry) { selected = entry }
                }
            }
            item(key = "bottom") { Spacer(Modifier.height(16.dp)) }
        }
    }

    selected?.let { entry ->
        UpgradeApplySheet(
            hub = hub,
            entry = entry,
            onClose = { selected = null },
            onApply = { job ->
                onApply(job)
                selected = null
            },
        )
    }
}

@Composable
private fun ProtocolStrip(onApply: (Job) -> Unit) {
    val active = Xin.prefs.currentSecondForm.value
    val protected = Xin.prefs.isApplied(Config.PROTECTION_JOB)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Cyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Security, contentDescription = null, tint = Cyan, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (active == null) "No Second Form active" else "Active · ${active.substringAfterLast('-')}",
                color = TextHi,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (protected) "Restoration protection is on" else "Protection needed before install",
                color = if (protected) Cyan else TextLow,
                fontSize = 9.sp,
                maxLines = 1,
            )
        }
        GhostAction(if (protected) "ON" else "ENABLE") {
            if (!protected) onApply(protectionJob())
        }
    }
}

@Composable
private fun FeaturedUpgrade(entry: UpgradeEntry, onOpen: (UpgradeEntry) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Line)
            .pressable { onOpen(entry) },
    ) {
        MenuThumb(
            entry.image ?: entry.portrait,
            Modifier.fillMaxSize(),
            ContentScale.Crop,
            720,
            420,
            16.dp,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xF2080B10), Color(0xB2080B10), Color(0x11080B10))
                    )
                ),
        )
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Pill("SECOND FORM", Cyan, filled = true)
                Pill("FREE", Cyan)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                entry.name,
                color = TextHi,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                fontFamily = XinDisplay,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text("${entry.hero} · advanced route", color = Color(0xFFD7DBE4), fontSize = 10.sp)
            Spacer(Modifier.height(9.dp))
            PrimaryAction("VIEW UPGRADE", icon = Icons.Rounded.AutoAwesome) { onOpen(entry) }
        }
    }
}

@Composable
private fun SecondFormTile(entry: UpgradeEntry, onClick: (UpgradeEntry) -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 148.dp, height = 186.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .border(1.dp, Cyan.copy(alpha = 0.34f), RoundedCornerShape(14.dp))
            .pressable { onClick(entry) },
    ) {
        MenuThumb(
            entry.image ?: entry.portrait,
            Modifier.fillMaxSize(),
            ContentScale.Crop,
            360,
            460,
            14.dp,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xEE080B10)))),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
        ) {
            Pill(entry.tier.ifBlank { "upgrade" }, tierColor(entry.tier), filled = true)
            Spacer(Modifier.height(5.dp))
            Text(
                entry.name,
                color = TextHi,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(entry.hero, color = TextLow, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun UpgradeRow(entry: UpgradeEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(1.dp, if (entry.secondForm) Cyan.copy(alpha = 0.38f) else Line, RoundedCornerShape(12.dp))
            .pressable(onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MenuThumb(
            entry.portrait ?: entry.image,
            Modifier.size(width = 48.dp, height = 62.dp),
            ContentScale.Crop,
            240,
            300,
            9.dp,
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Pill(entry.tier.ifBlank { "upgrade" }, tierColor(entry.tier))
                if (entry.secondForm) Pill("second form", Cyan)
                if (entry.injectable) Pill("ready", Cyan)
            }
            Spacer(Modifier.height(5.dp))
            Text(
                entry.name,
                color = TextHi,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${entry.hero} · ${entry.tag.orEmpty().ifBlank { "upgrade" }}",
                color = TextLow,
                fontSize = 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextLow, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun UpgradeApplySheet(
    hub: HubState,
    entry: UpgradeEntry,
    onClose: () -> Unit,
    onApply: (Job) -> Unit,
) {
    val slots = remember(entry, hub.catalog.skins) {
        hub.catalog.skins
            .filter { it.heroSlug == entry.heroSlug && it.injectable && !it.secondForm }
            .sortedBy { it.name }
    }
    var query by remember { mutableStateOf("") }
    val shown by remember(slots, query) {
        derivedStateOf { slots.filter { query.isBlank() || it.name.contains(query, true) } }
    }

    val protected = Xin.prefs.isApplied(Config.PROTECTION_JOB)
    val active = Xin.prefs.currentSecondForm.value
    val sameActive = active?.startsWith("upgrade-${entry.id}") == true
    val needsRestore = active != null && !sameActive
    val needsProtection = entry.protectionRequired && !protected
    val blocked = needsRestore || needsProtection

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD6080B10))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(Panel)
                .clickable(enabled = false) {},
        ) {
            Spacer(Modifier.height(9.dp))
            SheetHandle()
            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Panel)
                        .border(1.dp, Line, RoundedCornerShape(9.dp))
                        .pressable(onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextMid, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (entry.secondForm) "Second Form upgrade" else "Upgrade skin",
                        color = TextHi,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        "${entry.hero} · ${entry.name}",
                        color = TextLow,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Pill(if (entry.secondForm) "FREE" else "LOOK", Cyan, filled = entry.secondForm)
            }

            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(142.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Line),
            ) {
                MenuThumb(
                    entry.image ?: entry.portrait,
                    Modifier.fillMaxSize(),
                    ContentScale.Crop,
                    640,
                    420,
                    13.dp,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xEE080B10)))),
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(11.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Pill(entry.tier.ifBlank { "upgrade" }, tierColor(entry.tier), filled = true)
                    if (entry.secondForm) Pill("SECOND FORM", Cyan, filled = true)
                }
            }

            Spacer(Modifier.height(9.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                RouteCell("ROUTE", entry.routeFile ?: "generic", Modifier.weight(1f))
                RouteCell("FORMAT", entry.routeFormat ?: "standard", Modifier.weight(1f))
            }

            if (blocked) {
                Spacer(Modifier.height(9.dp))
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                    Row(modifier = Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (needsRestore) Icons.Rounded.Restore else Icons.Rounded.Security,
                            contentDescription = null,
                            tint = Danger,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.width(9.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (needsRestore) "Active Second Form detected" else "Protection required",
                                color = TextHi,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (needsRestore) "Restore the current form before switching."
                                else "Enable protection before installing.",
                                color = TextLow,
                                fontSize = 9.sp,
                                maxLines = 1,
                            )
                        }
                        GhostAction(if (needsRestore) "RESTORE" else "ENABLE", color = Danger) {
                            if (needsRestore && active != null) hub.restore(active)
                            else onApply(protectionJob())
                            onClose()
                        }
                    }
                }
            }

            Spacer(Modifier.height(9.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                SearchField(query, "Choose the slot to upgrade") { query = it }
            }
            Spacer(Modifier.height(7.dp))

            if (shown.isEmpty()) {
                EmptyState("No slots", "This hero has no injectable slots yet.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize().weight(1f),
                ) {
                    items(shown, key = { it.id }) { skin ->
                        UpgradeSlotRow(skin, enabled = !blocked) {
                            entry.toJob(skin)?.let { job ->
                                onApply(job)
                                onClose()
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RouteCell(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(10.dp)),
    ) {
        Column(modifier = Modifier.padding(9.dp)) {
            Text(label, color = TextLow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(
                value,
                color = TextHi,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun UpgradeSlotRow(skin: Skin, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) Panel else Panel.copy(alpha = 0.55f))
            .border(1.dp, Line, RoundedCornerShape(10.dp))
            .pressable(enabled, onClick)
            .padding(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MenuThumb(skin.image, Modifier.size(42.dp), ContentScale.Crop, 200, 240, 8.dp)
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                skin.name,
                color = if (enabled) TextHi else TextLow,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Pill(skin.tier.ifBlank { "skin slot" }, tierColor(skin.tier))
        }
        Text(if (enabled) "USE" else "LOCKED", color = if (enabled) Cyan else TextLow, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
    }
}
