package official.xinhub.assistant.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.xinhub.assistant.R
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.Config
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.data.Skin
import official.xinhub.assistant.data.UpgradeEntry
import official.xinhub.assistant.data.toJob
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.EmptyState
import official.xinhub.assistant.ui.components.FilterChip
import official.xinhub.assistant.ui.components.GlassCard
import official.xinhub.assistant.ui.components.NetImage
import official.xinhub.assistant.ui.components.Pill
import official.xinhub.assistant.ui.components.SearchField
import official.xinhub.assistant.ui.components.pressable
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Danger
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.Violet
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
    var filter by rememberSaveable { mutableStateOf("all") }
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<UpgradeEntry?>(null) }

    val secondForms = remember(upgrades) { upgrades.count { it.secondForm } }
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Panel)
                    .pressable(onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextMid, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "UPGRADE SKINS",
                    color = TextHi,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = XinDisplay,
                    letterSpacing = 0.6.sp,
                )
                Text("Hierarchical catalog · ${upgrades.size} looks", color = Cyan, fontSize = 9.5.sp)
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(136.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Cyan.copy(alpha = 0.28f), RoundedCornerShape(16.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.upgrade_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xE6080B10), Color(0x99080B10), Color(0x22080B10))
                        )
                    ),
            )
            Column(
                modifier = Modifier.align(Alignment.CenterStart).padding(14.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Pill("SECOND FORM", Cyan, filled = true)
                Spacer(Modifier.height(7.dp))
                Text(
                    "$secondForms free Second Forms",
                    color = TextHi,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = XinDisplay,
                )
                Text(
                    "Free in XIN HUB · no key needed",
                    color = Color(0xFFD7DBE4),
                    fontSize = 10.5.sp,
                )
            }
        }

        Spacer(Modifier.height(9.dp))
        GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Violet.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Violet, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Second Form status", color = TextHi, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            Xin.prefs.currentSecondForm.value?.let { "Active · ${it.substringAfterLast('-')}" }
                                ?: "No Second Form active",
                            color = if (Xin.prefs.currentSecondForm.value == null) TextLow else Cyan,
                            fontSize = 10.sp,
                        )
                    }
                    Pill(if (Xin.prefs.isApplied(Config.PROTECTION_JOB)) "protected" else "unprotected",
                        if (Xin.prefs.isApplied(Config.PROTECTION_JOB)) Cyan else Danger)
                }
                Spacer(Modifier.height(9.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Panel)
                        .border(1.dp, Line, RoundedCornerShape(10.dp))
                        .clickable { onApply(protectionJob()) }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = TextMid, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(9.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (Xin.prefs.isApplied(Config.PROTECTION_JOB)) "Restoration Protection enabled"
                            else "Enable Restoration Protection",
                            color = TextHi,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text("Required before Second Form installs", color = TextLow, fontSize = 9.sp)
                    }
                    Text(
                        if (Xin.prefs.isApplied(Config.PROTECTION_JOB)) "ON" else "ENABLE",
                        color = Cyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(9.dp))
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            SearchField(query, "Search upgrade skins") { query = it }
        }
        Spacer(Modifier.height(7.dp))
        LazyRowChips(
            filters = listOf(
                "all" to "All · ${upgrades.size}",
                "second" to "Second Form · $secondForms",
                "regular" to "Regular · ${upgrades.size - secondForms}",
            ),
            selected = filter,
            onSelect = { filter = it },
        )

        Spacer(Modifier.height(8.dp))
        if (shown.isEmpty()) {
            EmptyState("No upgrade skins", "Try another filter or clear the search.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.fillMaxSize().weight(1f),
            ) {
                items(shown, key = { it.id }) { entry ->
                    UpgradeCard(entry) { selected = entry }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
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
private fun LazyRowChips(
    filters: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        filters.forEach { (id, label) ->
            Box(modifier = Modifier.weight(1f)) {
                FilterChip(label, id == selected) { onSelect(id) }
            }
        }
    }
}

@Composable
private fun UpgradeCard(entry: UpgradeEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Panel)
            .border(1.dp, if (entry.secondForm) Cyan.copy(alpha = 0.38f) else Line, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick)
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 78.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Line),
        ) {
            NetImage(
                entry.image ?: entry.portrait,
                Modifier.fillMaxSize(),
                targetWidth = 456,
                targetHeight = 534,
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Pill(entry.tier.ifBlank { "upgrade" }, tierColor(entry.tier))
                if (entry.secondForm) Pill("second form", Cyan)
            }
            Spacer(Modifier.height(5.dp))
            Text(
                entry.name,
                color = TextHi,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("${entry.hero} · ${entry.tag.orEmpty().ifBlank { "upgrade" }}", color = TextLow, fontSize = 10.sp, maxLines = 1)
        }
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 34.dp)
                .clip(RoundedCornerShape(50))
                .background(Cyan.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.upgrade_tag),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(22.dp),
            )
        }
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
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 34.dp, height = 3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Line),
            )
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Line)
                        .pressable(onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextMid, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (entry.secondForm) "Second Form upgrade" else "Upgrade skin",
                        color = TextHi,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("${entry.hero} · ${entry.name}", color = TextLow, fontSize = 10.sp, maxLines = 1)
                }
            }

            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(148.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Line),
            ) {
                NetImage(
                    entry.image ?: entry.portrait,
                    Modifier.fillMaxSize(),
                    targetWidth = 640,
                    targetHeight = 420,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xD6080B10)))),
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
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
                StatusCell("ROUTE", entry.routeFile ?: "generic", Modifier.weight(1f))
                StatusCell("FORMAT", entry.routeFormat ?: "standard", Modifier.weight(1f))
            }

            if (blocked) {
                Spacer(Modifier.height(9.dp))
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                    Row(modifier = Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = Danger, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(9.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (needsRestore) "Active Second Form detected" else "Protection required",
                                color = TextHi,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (needsRestore) "Restore the current Second Form before switching."
                                else "Enable Restoration Protection before installing.",
                                color = TextLow,
                                fontSize = 9.5.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Danger.copy(alpha = 0.14f))
                                .clickable {
                                    if (needsRestore && active != null) hub.restore(active)
                                    else onApply(protectionJob())
                                    onClose()
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                        ) {
                            Text(if (needsRestore) "RESTORE" else "ENABLE", color = Danger, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
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
                        SlotRow(
                            skin = skin,
                            enabled = !blocked,
                            onClick = {
                                entry.toJob(skin)?.let { job ->
                                    onApply(job)
                                    onClose()
                                }
                            },
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatusCell(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(9.dp)) {
            Text(label, color = TextLow, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Text(
                value,
                color = TextHi,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SlotRow(skin: Skin, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) Panel else Panel.copy(alpha = 0.55f))
            .border(1.dp, Line, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Line)) {
            NetImage(skin.image, Modifier.fillMaxSize(), targetWidth = 240, targetHeight = 280)
        }
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
            Text(skin.tier.ifBlank { "skin slot" }, color = TextMid, fontSize = 9.5.sp)
        }
        if (enabled) Text("USE", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
