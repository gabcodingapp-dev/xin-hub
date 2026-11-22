package official.nixhub.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import official.nixhub.assistant.data.EffectItem
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.data.toJob
import official.nixhub.assistant.ui.HubState
import official.nixhub.assistant.ui.components.EmptyState
import official.nixhub.assistant.ui.components.FilterChip
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.components.SearchField
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.XinDisplay

@Composable
fun EffectsScreen(hub: HubState, onApply: (Job) -> Unit, kind: String? = null) {
    val isPrep = kind == "prep" || kind == "intro"
    val groups = remember(hub.catalog.effects, kind) {
        hub.catalog.effects.filter { cat ->
            when {
                isPrep -> cat.kind == "intro" || cat.kind == "background"
                else -> cat.kind == "effect"
            }
        }
    }
    var selected by rememberSaveable(kind) { mutableStateOf(groups.firstOrNull()?.id ?: "") }
    var query by rememberSaveable(kind) { mutableStateOf("") }

    val active = groups.firstOrNull { it.id == selected } ?: groups.firstOrNull()
    val shown by remember(active, query) {
        derivedStateOf {
            active?.items?.filter { query.isBlank() || it.name.contains(query, true) } ?: emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 14.dp).padding(top = 10.dp)) {
            Text(
                if (isPrep) "PREPARATION" else "EFFECTS",
                color = TextHi,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                fontFamily = XinDisplay,
                letterSpacing = 1.sp,
            )
            Text(
                if (isPrep) "${groups.sumOf { it.items.size }} loading screens, backgrounds and more"
                else "${groups.sumOf { it.items.size }} recalls, emotes, trails and more",
                color = TextLow,
                fontSize = 10.5.sp,
            )
            Spacer(Modifier.height(9.dp))
            SearchField(query, if (isPrep) "Search prep" else "Search effects") { query = it }
        }

        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(groups, key = { it.id }) { cat ->
                FilterChip("${cat.title} · ${cat.items.size}", cat.id == active?.id) { selected = cat.id }
            }
        }

        Spacer(Modifier.height(10.dp))
        if (shown.isEmpty()) {
            EmptyState("Nothing here", "Try another category or clear the search.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shown, key = { it.id }) { item ->
                    EffectTile(item) {
                        val job = active?.let { item.toJob(it) }
                        if (job != null) onApply(job) else hub.say("${item.name} has no script")
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectTile(item: EffectItem, onClick: () -> Unit) {
    val applied = Xin.prefs.isApplied(item.id)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(11.dp))
            .clickable { onClick() },
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.05f)) {
            NetImage(item.image, Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xDD090A0E)))),
            )
            if (applied) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Cyan)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                ) {
                    Text("on", color = Color(0xFF090A0E), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                item.name,
                color = TextHi,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}
