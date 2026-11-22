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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.components.Pill
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.Violet

@Composable
fun EffectsScreen(
    hub: HubState,
    onApply: (Job) -> Unit,
    kind: String? = null,
) {
    val catalog = hub.catalog
    val categories = remember(catalog.effects, kind) {
        if (kind == null) catalog.effects.filter { it.kind == "effect" }
        else catalog.effects.filter { it.kind == kind }
    }
    var selected by rememberSaveable(kind) { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var query by rememberSaveable { mutableStateOf("") }

    val active = categories.firstOrNull { it.id == selected } ?: categories.firstOrNull()
    val items = remember(active, query) {
        active?.items?.filter { query.isBlank() || it.name.contains(query, true) } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp)) {
            Text(
                if (kind == "intro") "Loading Intros" else "Effects",
                color = TextHi,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.6).sp,
            )
            Text(
                if (kind == "intro") "Animated loading screens, previews included"
                else "Recalls, emotes, trails, eliminations and more",
                color = TextLow,
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(12.dp))
            SearchField(query, "Search ${if (kind == "intro") "intros" else "effects"}") { query = it }
        }

        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories, key = { it.id }) { cat ->
                FilterChip("${cat.title} · ${cat.items.size}", cat.id == selected, Violet) {
                    selected = cat.id
                }
            }
        }

        if (active != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(active.description, color = TextLow, fontSize = 10.5.sp, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            EmptyState("Nothing here", "Try another category or clear the search.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items, key = { it.id }) { item ->
                    EffectCard(item, active?.title ?: "") {
                        val job = item.toJob(active!!)
                        if (job != null) onApply(job) else hub.say("${item.name} has no script")
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectCard(item: EffectItem, category: String, onClick: () -> Unit) {
    val applied = Xin.prefs.isApplied(item.id)
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
                .aspectRatio(1.15f)
        ) {
            NetImage(url = item.image, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color(0x00000000), Color(0xCC0C0A18))))
            )
            if (applied) {
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
                    item.name,
                    color = TextHi,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (category.isNotBlank()) {
                    Text(category, color = TextMid, fontSize = 9.5.sp, maxLines = 1)
                }
            }
        }
    }
}
