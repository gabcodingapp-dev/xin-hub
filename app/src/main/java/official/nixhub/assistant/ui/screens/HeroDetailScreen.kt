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
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.Xin
import official.nixhub.assistant.data.Skin
import official.nixhub.assistant.data.toJob
import official.nixhub.assistant.ui.HubState
import official.nixhub.assistant.ui.components.EmptyState
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.components.Pill
import official.nixhub.assistant.ui.components.SearchField
import official.nixhub.assistant.ui.components.pressable
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.tierColor
import official.nixhub.assistant.ui.theme.XinDisplay

private val GROUPS = listOf("visual", "upgrade", "custom")
private val GROUP_LABEL = mapOf("visual" to "Visual", "upgrade" to "Upgrade", "custom" to "Custom")

@Composable
fun HeroDetailScreen(
    hub: HubState,
    slug: String,
    onBack: () -> Unit,
    onReplace: (String) -> Unit,
    onUpgrade: (Skin) -> Unit,
) {
    val hero = remember(slug, hub.catalog.heroes) { hub.catalog.heroes.firstOrNull { it.slug == slug } }
    val all = remember(slug, hub.catalog.skins) { hub.catalog.skins.filter { it.heroSlug == slug } }
    var tab by rememberSaveable(slug) { mutableStateOf("visual") }
    var query by rememberSaveable(slug) { mutableStateOf("") }

    val shown by remember(all, tab, query) {
        derivedStateOf {
            val pool = if (tab == "upgrade") all.filter { it.injectable }
                       else all.filter { it.group == tab }
            pool.filter { query.isBlank() || it.name.contains(query, true) }
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
                Text(hero?.name ?: slug, color = TextHi, fontSize = 19.sp, fontWeight = FontWeight.Black, fontFamily = XinDisplay)
                Text(
                    hero?.roles?.joinToString(" · ") ?: "",
                    color = Cyan,
                    fontSize = 9.5.sp,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            GROUPS.forEach { group ->
                val count = if (group == "upgrade") all.count { it.injectable } else all.count { it.group == group }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (tab == group) Cyan else Panel)
                        .pressable { tab = group }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${GROUP_LABEL[group]} · $count",
                        color = if (tab == group) Color(0xFF090A0E) else TextMid,
                        fontSize = 10.sp,
                        fontWeight = if (tab == group) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(Modifier.height(9.dp))
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(Cyan, Color(0xFF7CE7FF))))
                    .pressable { onReplace(slug) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = Color(0xFF090A0E), modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Replace a skin", color = Color(0xFF090A0E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            if (tab == "upgrade") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Panel)
                        .border(1.dp, Cyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                ) {
                    Text(
                        "Universal upgrade · every injectable skin can be used here. Pick a look, then choose the skin slot to place it on.",
                        color = TextMid,
                        fontSize = 10.sp,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            SearchField(query, "Search ${GROUP_LABEL[tab]} skins") { query = it }
        }

        Spacer(Modifier.height(8.dp))
        if (shown.isEmpty()) {
            EmptyState("No skins here", "This hero has no ${GROUP_LABEL[tab]} entries yet.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shown, key = { it.id }) { skin ->
                    SkinRow(skin, tab == "upgrade") {
                        if (tab == "upgrade") {
                            onUpgrade(skin)
                        } else {
                            val job = skin.toJob()
                            if (job != null) hub.apply(job) else hub.say("${skin.name} has no script yet")
                        }
                    }
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable
private fun SkinRow(skin: Skin, upgradeMode: Boolean, onClick: () -> Unit) {
    val applied = Xin.prefs.isApplied(skin.id)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(11.dp))
            .pressable(onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))) {
            NetImage(skin.image, Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                skin.name,
                color = TextHi,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Pill(skin.tier.ifBlank { "skin" }, tierColor(skin.tier))
                if (upgradeMode || skin.group == "upgrade") Pill("upgrade", Cyan)
                if (skin.group == "custom") Pill("custom", Cyan)
                if (applied) Pill("on", Cyan, filled = true)
            }
        }
        if (skin.injectable) {
            Text(if (upgradeMode) "UPGRADE" else "USE", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("soon", color = TextLow, fontSize = 9.5.sp)
        }
    }
}
