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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.data.Skin
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.EmptyState
import official.xinhub.assistant.ui.components.MenuThumb
import official.xinhub.assistant.ui.components.Pill
import official.xinhub.assistant.ui.components.SearchField
import official.xinhub.assistant.ui.components.SheetHandle
import official.xinhub.assistant.ui.components.pressable
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.tierColor

@Composable
fun SlotSheet(hub: HubState, slug: String, onClose: () -> Unit, preset: Skin? = null) {
    val upgradeMode = preset != null
    val hero = remember(slug, hub.catalog.heroes) {
        hub.catalog.heroes.firstOrNull { it.slug == slug }
    }
    val skins = remember(slug, hub.catalog.skins) {
        hub.catalog.skins.filter { it.heroSlug == slug }
    }
    val owned = remember(skins) {
        skins.filter { !it.upgrade && it.source != "custom" }
            .ifEmpty { skins.filter { it.injectable } }
    }
    val targets = remember(skins) { skins.filter { it.injectable } }

    var step by remember { mutableStateOf(1) }
    var slot by remember { mutableStateOf<Skin?>(null) }
    var query by remember { mutableStateOf("") }

    val pool = if (upgradeMode || step == 1) owned else targets
    val shown by remember(pool, query) {
        derivedStateOf { pool.filter { query.isBlank() || it.name.contains(query, true) } }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC080B10))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
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
                        .pressable {
                            if (step == 2 && !upgradeMode) {
                                step = 1
                                slot = null
                                query = ""
                            } else onClose()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (step == 2 && !upgradeMode) Icons.Rounded.ArrowBack else Icons.Rounded.Close,
                        contentDescription = null,
                        tint = TextMid,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (upgradeMode) "Choose skin slot" else if (step == 1) "Which skin do you own?" else "Use this skin",
                        color = TextHi,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        if (upgradeMode) "${preset?.name ?: "Upgrade"} · tap a slot to inject"
                        else if (step == 1) "${hero?.name ?: slug} · choose the slot to replace"
                        else "Replacing ${slot?.name ?: ""} · ${targets.size} looks",
                        color = TextLow,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Pill(
                    if (upgradeMode) "UPGRADE" else "STEP $step/2",
                    Cyan,
                    filled = upgradeMode,
                )
            }

            if (upgradeMode && preset != null) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(132.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Line),
                ) {
                    MenuThumb(
                        preset.splash ?: preset.image,
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
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UPGRADE LOOK", color = Cyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                preset.name,
                                color = TextHi,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Pill(preset.tier.ifBlank { "skin" }, tierColor(preset.tier), filled = true)
                    }
                }
            } else if (step == 2 && slot != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Cyan.copy(alpha = 0.08f))
                        .border(1.dp, Cyan.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MenuThumb(slot?.image, Modifier.size(32.dp), ContentScale.Crop, 180, 200, 8.dp)
                    Spacer(Modifier.width(9.dp))
                    Text("Replacing", color = TextLow, fontSize = 9.5.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        slot?.name ?: "",
                        color = TextHi,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                SearchField(query, if (upgradeMode || step == 1) "Search skin slots" else "Search skins to use") { query = it }
            }
            Spacer(Modifier.height(7.dp))

            if (shown.isEmpty()) {
                EmptyState("Nothing here", "Try a different search or hero.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(shown, key = { it.id }) { skin ->
                        SheetSkinRow(skin, selected = slot?.id == skin.id) {
                            if (upgradeMode) {
                                val look = preset ?: return@SheetSkinRow
                                hub.apply(
                                    Job(
                                        id = "upgrade-" + look.id + "-" + skin.id,
                                        kind = Job.Kind.SKIN,
                                        title = look.name,
                                        subtitle = "${look.hero} · over ${skin.name}",
                                        script = look.bestScript ?: "",
                                        image = look.splash ?: look.image,
                                        slotId = skin.id,
                                        slotName = skin.name,
                                        upgradeMode = true,
                                        secondForm = look.secondForm,
                                        protectionRequired = look.advanced?.restorationProtectionRequired ?: false,
                                    )
                                )
                                onClose()
                            } else if (step == 1) {
                                slot = skin
                                step = 2
                                query = ""
                            } else {
                                val chosen = slot ?: return@SheetSkinRow
                                hub.apply(
                                    Job(
                                        id = "slot-" + chosen.id + "-" + skin.id,
                                        kind = Job.Kind.SKIN,
                                        title = skin.name,
                                        subtitle = "${skin.hero} · over ${chosen.name}",
                                        script = skin.bestScript ?: "",
                                        image = skin.splash ?: skin.image,
                                        slotId = chosen.id,
                                        slotName = chosen.name,
                                        upgradeMode = false,
                                    )
                                )
                                onClose()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetSkinRow(skin: Skin, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Cyan.copy(alpha = 0.10f) else Panel)
            .border(1.dp, if (selected) Cyan else Line, RoundedCornerShape(10.dp))
            .pressable(onClick)
            .padding(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MenuThumb(skin.image, Modifier.size(42.dp), ContentScale.Crop, 200, 240, 8.dp)
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                skin.name,
                color = if (selected) Cyan else TextHi,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Pill(skin.tier.ifBlank { "skin" }, tierColor(skin.tier))
                if (skin.injectable) Pill("ready", Cyan)
            }
        }
        Text(if (selected) "PICKED" else "USE", color = Cyan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
    }
}
