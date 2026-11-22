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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
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
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.Config
import official.xinhub.assistant.data.EffectItem
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.data.toJob
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.EmptyState
import official.xinhub.assistant.ui.components.FilterChip
import official.xinhub.assistant.ui.components.GlassCard
import official.xinhub.assistant.ui.components.NetImage
import official.xinhub.assistant.ui.components.SearchField
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.XinDisplay

@Composable
fun EffectsScreen(hub: HubState, onApply: (Job) -> Unit, kind: String? = null) {
    val isGb = kind == "gb" || kind == "intro"
    val isPrep = isGb || kind == "prep"
    val groups = remember(hub.catalog.effects, kind) {
        hub.catalog.effects.filter { cat ->
            when {
                isGb -> cat.kind == "intro"
                isPrep -> cat.kind == "intro" || cat.kind == "background"
                else -> cat.kind == "effect"
            }
        }
    }
    var selected by rememberSaveable(kind) { mutableStateOf(groups.firstOrNull()?.id ?: "") }
    var query by rememberSaveable(kind) { mutableStateOf("") }
    var preview by remember { mutableStateOf<String?>(null) }

    val active = groups.firstOrNull { it.id == selected } ?: groups.firstOrNull()
    val shown by remember(active, query) {
        derivedStateOf {
            active?.items?.filter { query.isBlank() || it.name.contains(query, true) } ?: emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 14.dp).padding(top = 10.dp)) {
            Text(
                when {
                    isGb -> "GB LOADING SCREEN"
                    isPrep -> "PREPARATION"
                    else -> "EFFECTS"
                },
                color = TextHi,
                fontSize = if (isGb) 21.sp else 23.sp,
                fontWeight = FontWeight.Black,
                fontFamily = XinDisplay,
                letterSpacing = 1.sp,
            )
            Text(
                when {
                    isGb -> "${groups.sumOf { it.items.size }} screens · tap play for a live preview"
                    isPrep -> "${groups.sumOf { it.items.size }} loading screens, backgrounds and more"
                    else -> "${groups.sumOf { it.items.size }} recalls, emotes, trails and more"
                },
                color = TextLow,
                fontSize = 10.5.sp,
            )
            Spacer(Modifier.height(9.dp))
            SearchField(query, if (isGb) "Search loading screens" else if (isPrep) "Search prep" else "Search effects") {
                query = it
            }
        }

        if (isGb) {
            Spacer(Modifier.height(9.dp))
            RestoreIntroCard(
                onApply = {
                    onApply(
                        Job(
                            id = "gb-restore-original",
                            kind = Job.Kind.EFFECT,
                            title = "Restore Original MLBB Intro",
                            subtitle = "GB loading screen · stock",
                            script = Config.GB_RESTORE_SCRIPT,
                            image = Config.GB_RESTORE_IMAGE,
                            intro = true,
                        )
                    )
                },
            )
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
                    EffectTile(
                        item = item,
                        onClick = {
                            val job = active?.let { item.toJob(it) }
                            if (job != null) onApply(job) else hub.say("${item.name} has no script")
                        },
                        onPreview = { url -> preview = url },
                    )
                }
            }
        }
    }

    preview?.let { url ->
        IntroPreviewDialog(url = url, title = "Loading screen preview", onClose = { preview = null })
    }
}

@Composable
private fun RestoreIntroCard(onApply: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onApply)
                .padding(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 62.dp, height = 46.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Panel),
            ) {
                NetImage(Config.GB_RESTORE_IMAGE, Modifier.fillMaxSize(), targetWidth = 240, targetHeight = 180)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Restore original intro", color = TextHi, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Put the stock MLBB loading screen back", color = TextLow, fontSize = 9.5.sp, maxLines = 1)
            }
            Icon(Icons.Rounded.Restore, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun EffectTile(item: EffectItem, onClick: () -> Unit, onPreview: (String) -> Unit) {
    val applied = Xin.prefs.isApplied(item.id)
    val preview = item.preview?.takeIf { it.isNotBlank() }
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
            if (preview != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(25.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xCC0B0E14))
                        .clickable { onPreview(preview) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Preview",
                        tint = Cyan,
                        modifier = Modifier.size(15.dp),
                    )
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
            if (item.views > 0) {
                Text(
                    compactViews(item.views),
                    color = TextLow,
                    fontSize = 8.sp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(horizontal = 7.dp, vertical = 5.dp),
                )
            }
        }
    }
}

private fun compactViews(value: Int): String = when {
    value >= 1_000_000 -> "${value / 1_000_000}m"
    value >= 1_000 -> "${value / 1_000}k"
    else -> "$value"
}
