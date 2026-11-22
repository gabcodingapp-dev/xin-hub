package official.nixhub.assistant.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.data.Skin
import official.nixhub.assistant.ui.HubState
import official.nixhub.assistant.ui.components.EmptyState
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.components.SearchField
import official.nixhub.assistant.ui.components.pressable
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid

/**
 * Two-step replacement flow: choose the skin slot the player already owns, then the look to put
 * in it. The slot is recorded on the job so Library shows what was swapped where.
 */
@Composable
fun SlotSheet(hub: HubState, slug: String, onClose: () -> Unit, preset: Skin? = null) {
    val upgradeMode = preset != null
    val hero = remember(slug, hub.catalog.heroes) { hub.catalog.heroes.firstOrNull { it.slug == slug } }
    val skins = remember(slug, hub.catalog.skins) { hub.catalog.skins.filter { it.heroSlug == slug } }
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
            .background(Color(0xCC090A0E))
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
                        if (step == 2) Icons.Rounded.ArrowBack else Icons.Rounded.Close,
                        contentDescription = null,
                        tint = TextMid,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (upgradeMode) "Choose the skin to upgrade"
                        else if (step == 1) "Which skin do you own?" else "Use this skin",
                        color = TextHi,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (upgradeMode) "${preset?.name ?: "Upgrade"} · tap a slot to inject"
                        else if (step == 1) "${hero?.name ?: slug} · choose the slot to replace"
                        else "Replacing ${slot?.name ?: ""} · ${targets.size} available",
                        color = TextLow,
                        fontSize = 10.sp,
                    )
                }
            }

            if (upgradeMode && preset != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Panel)
                        .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(11.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))) {
                        NetImage(preset.image, Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("UPGRADE LOOK", color = Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(preset.name, color = TextHi, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                SearchField(query, if (upgradeMode) "Search skin slots" else if (step == 1) "Search owned skins" else "Search skins to use") { query = it }
            }
            Spacer(Modifier.height(8.dp))

            if (shown.isEmpty()) {
                EmptyState("Nothing here", "Try a different search or hero.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(shown, key = { it.id }) { skin ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (slot?.id == skin.id) Cyan.copy(alpha = 0.12f) else Panel)
                                .border(
                                    1.dp,
                                    if (slot?.id == skin.id) Cyan else Line,
                                    RoundedCornerShape(10.dp),
                                )
                                .pressable {
                                    if (upgradeMode) {
                                        val look = preset ?: return@pressable
                                        hub.apply(
                                            Job(
                                                id = "upgrade-" + look.id + "-" + skin.id,
                                                kind = Job.Kind.SKIN,
                                                title = look.name,
                                                subtitle = "${look.hero} · over ${skin.name}",
                                                script = look.script ?: "",
                                                image = look.image,
                                                slotId = skin.id,
                                                slotName = skin.name,
                                                upgradeMode = true,
                                            )
                                        )
                                        onClose()
                                    } else if (step == 1) {
                                        slot = skin
                                        step = 2
                                        query = ""
                                    } else {
                                        val chosen = slot ?: return@pressable
                                        hub.apply(
                                            Job(
                                                id = "slot-" + chosen.id + "-" + skin.id,
                                                kind = Job.Kind.SKIN,
                                                title = skin.name,
                                                subtitle = "${skin.hero} · into ${chosen.name}",
                                                script = skin.script ?: "",
                                                image = skin.image,
                                                slotId = chosen.id,
                                                slotName = chosen.name,
                                            )
                                        )
                                        onClose()
                                    }
                                }
                                .padding(7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(8.dp))) {
                                NetImage(skin.image, Modifier.fillMaxSize())
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    skin.name,
                                    color = TextHi,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    if (upgradeMode || step == 1) skin.tier.ifBlank { "skin slot" }
                                    else if (skin.upgrade) "upgrade" else if (skin.source == "custom") "custom" else "visual",
                                    color = TextMid,
                                    fontSize = 9.5.sp,
                                )
                            }
                            if (!upgradeMode && step == 2) {
                                Text("USE", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (!upgradeMode && step == 1 && slot != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Cyan, Color(0xFF7CE7FF))))
                        .clickable { step = 2 }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Next: choose the look", color = Color(0xFF090A0E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
