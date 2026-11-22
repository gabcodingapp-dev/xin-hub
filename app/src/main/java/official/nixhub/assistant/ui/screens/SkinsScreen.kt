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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.Xin
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.data.Skin
import official.nixhub.assistant.data.toJob
import official.nixhub.assistant.ui.HubState
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.components.Pill
import official.nixhub.assistant.ui.components.EmptyState
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.PanelHi
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.Violet
import official.nixhub.assistant.ui.theme.tierColor

@Composable
fun SkinsScreen(hub: HubState, onApply: (Job) -> Unit) {
    val catalog = hub.catalog
    var query by rememberSaveable { mutableStateOf("") }
    var hero by rememberSaveable { mutableStateOf("") }
    var onlyReady by rememberSaveable { mutableStateOf(false) }
    var onlyTag by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf("") }

    val filtered = remember(catalog.skins, query, hero, onlyReady, onlyTag, group) {
        catalog.skins.filter { skin ->
            (!onlyReady || skin.injectable) &&
                (group.isBlank() || skin.group == group) &&
                (hero.isBlank() || skin.hero == hero) &&
                (onlyTag.isBlank() || skin.tag == onlyTag) &&
                (query.isBlank() ||
                    skin.name.contains(query, true) ||
                    skin.hero.contains(query, true) ||
                    skin.tier.contains(query, true))
        }
    }

    val heroes = remember(catalog.skins) {
        catalog.skins.groupBy { it.hero }.entries.sortedBy { it.key }.map { it.key }
    }
    val tags = remember(catalog.tags) { catalog.tags }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp)) {
            Text(
                "Hero Skins",
                color = TextHi,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.6).sp,
            )
            Text(
                "${catalog.injectableCount} injectable · ${catalog.skins.size} catalogued",
                color = TextLow,
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(12.dp))
            SearchField(query, "Search hero, skin, tier") { query = it }
        }

        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { FilterChip("Ready only", onlyReady, Cyan) { onlyReady = !onlyReady } }
            item { FilterChip("All", group.isBlank(), Cyan) { group = "" } }
            item { FilterChip("Upgrade", group == "upgrade", Violet) { group = if (group == "upgrade") "" else "upgrade" } }
            item { FilterChip("Custom", group == "custom", Violet) { group = if (group == "custom") "" else "custom" } }
            item { FilterChip("All heroes", hero.isBlank(), Violet) { hero = "" } }
            items(heroes, key = { it }) { name ->
                FilterChip(name, hero == name, Violet) {
                    hero = if (hero == name) "" else name
                }
            }
        }

        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { FilterChip("Any tag", onlyTag.isBlank(), Cyan) { onlyTag = "" } }
                items(tags, key = { it.id }) { tag ->
                    FilterChip(tag.name, onlyTag == tag.id, Cyan) {
                        onlyTag = if (onlyTag == tag.id) "" else tag.id
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) {
            EmptyState("Nothing matches", "Loosen the filters or clear the search.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(filtered, key = { it.id }) { skin ->
                    SkinCard(skin) {
                        val job = skin.toJob()
                        if (job != null) onApply(job) else hub.say("${skin.hero} · ${skin.name} has no script yet")
                    }
                }
            }
        }
    }
}

@Composable
fun SkinCard(skin: Skin, onClick: () -> Unit) {
    val applied = Xin.prefs.isApplied(skin.id)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.86f)
        ) {
            NetImage(url = skin.image, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xCC0C0A18))))
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(7.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Pill(skin.tier.ifBlank { "skin" }, tierColor(skin.tier), filled = true)
                if (skin.group == "upgrade") Pill("upgrade", Cyan)
                if (skin.group == "custom") Pill("custom", Violet)
            }
            if (!skin.injectable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC1A1730))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("soon", color = TextLow, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
            } else if (applied) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Cyan.copy(alpha = 0.9f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("on", color = Color(0xFF07060F), fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 9.dp, vertical = 8.dp)
            ) {
                Text(
                    skin.name,
                    color = TextHi,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    skin.hero,
                    color = TextMid,
                    fontSize = 10.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun SearchField(value: String, hint: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PanelHi)
            .border(1.dp, Line.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Search,
            contentDescription = null,
            tint = TextLow,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(9.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(hint, color = TextLow, fontSize = 12.5.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = TextHi, fontSize = 12.5.sp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                tint = TextLow,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onChange("") },
            )
        }
    }
}

@Composable
fun FilterChip(label: String, active: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) accent.copy(alpha = 0.16f) else PanelHi)
            .border(
                1.dp,
                if (active) accent.copy(alpha = 0.7f) else Line.copy(alpha = 0.6f),
                RoundedCornerShape(50),
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = if (active) accent else TextMid,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
