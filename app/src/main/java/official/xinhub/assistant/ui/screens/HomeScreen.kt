package official.xinhub.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.Config
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.data.LoadState
import official.xinhub.assistant.data.toJob
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.GlassCard
import official.xinhub.assistant.ui.components.NetImage
import official.xinhub.assistant.ui.components.Pill
import official.xinhub.assistant.ui.components.SectionHeader
import official.xinhub.assistant.ui.components.StatChip
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.PanelHi
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.Violet
import official.xinhub.assistant.ui.theme.tierColor

private data class Quick(val route: String, val title: String, val sub: String, val icon: ImageVector, val tint: Color)

private val QUICK = listOf(
    Quick("skins", "Skin Changer", "874 ready · 1550 total", Icons.Rounded.Style, Violet),
    Quick("effects", "Effects", "Recalls · emotes", Icons.Rounded.AutoAwesome, Cyan),
    Quick("gb", "GB Loading Screen", "487 screens", Icons.Rounded.Movie, Color(0xFFF472B6)),
    Quick("upgrade", "Upgrade Skins", "9 Second Forms", Icons.Rounded.TrendingUp, Color(0xFFFBBF24)),
    Quick("lobby", "Lobby Sounds", "9 tracks", Icons.Rounded.MusicNote, Color(0xFFFBBF24)),
    Quick("library", "My Library", "Backups", Icons.Rounded.Inventory2, Color(0xFF34D399)),
    Quick("guides", "Guides", "Setup help", Icons.Rounded.LibraryBooks, Color(0xFF60A5FA)),
    Quick("restore", "Restore All", "Stock files", Icons.Rounded.Restore, Color(0xFFFF5C7A)),
    Quick("settings", "Setup", "SAF · Shizuku · root", Icons.Rounded.FolderOpen, Color(0xFFA78BFA)),
)

@Composable
fun HomeScreen(
    hub: HubState,
    onOpen: (String) -> Unit,
    onApply: (Job) -> Unit,
    onPickFolder: () -> Unit,
) {
    val catalog = hub.catalog
    val storage = hub.storage
    val applied = Xin.prefs.applied.value

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { HomeHeader(hub) }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (storage != null) Cyan.copy(alpha = 0.14f)
                                else Color(0xFFFF5C7A).copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = if (storage != null) Cyan else Color(0xFFFF5C7A),
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (storage != null) "Game folder linked" else "Game folder missing",
                            color = TextHi,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            storage?.label ?: "Point the app at Android/data/com.mobile.legends/files",
                            color = TextLow,
                            fontSize = 10.5.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        if (storage != null) "Change" else "Connect",
                        color = Cyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Cyan.copy(alpha = 0.10f))
                            .clickable { onPickFolder() }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatChip("skins", catalog.injectableCount.toString(), Modifier.weight(1f))
                StatChip("effects", catalog.effectCount.toString(), Modifier.weight(1f))
                StatChip("applied", applied.size.toString(), Modifier.weight(1f))
                StatChip("heroes", catalog.heroes.size.toString(), Modifier.weight(1f))
            }
        }

        item { QuickRow(QUICK.take(3), hub, onOpen) }
        item { QuickRow(QUICK.drop(3).take(3), hub, onOpen) }
        item { QuickRow(QUICK.drop(6), hub, onOpen) }

        if (catalog.latest.isNotEmpty()) {
            item {
                SectionHeader("Latest drops", "Freshest skins in the catalog", "All skins") { onOpen("skins") }
            }
            item {
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(catalog.latest.take(12), key = { it.id }) { item ->
                        Column(
                            modifier = Modifier
                                .width(128.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    val job = item.toJob(catalog.skins)
                                    if (job != null) onApply(job) else hub.say("${item.name} is not injectable yet")
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.78f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            ) {
                                NetImage(url = item.image, modifier = Modifier.fillMaxSize())
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                ) {
                                    Pill(item.tier.ifBlank { "skin" }, tierColor(item.tier), filled = true)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                item.name,
                                color = TextHi,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(item.hero, color = TextLow, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        if (catalog.tags.isNotEmpty()) {
            item { SectionHeader("Collections", "Browse by release tag") }
            item {
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(catalog.tags.take(24), key = { it.id }) { tag ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PanelHi)
                                .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(50))
                                .clickable { onOpen("skins") }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(50))
                            ) {
                                NetImage(tag.image, Modifier.fillMaxSize(), ContentScale.Crop)
                            }
                            Spacer(Modifier.width(7.dp))
                            Text(tag.name, color = TextMid, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = Violet,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Catalog", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            when (val s = hub.load) {
                                is LoadState.Ready -> "v${Config.VERSION} · ${s.source} · ${catalog.skins.size} skins"
                                is LoadState.Failed -> s.message
                                else -> "loading"
                            },
                            color = TextLow,
                            fontSize = 10.5.sp,
                        )
                    }
                    Icon(
                        Icons.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = TextLow,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { hub.refresh() },
                    )
                }
            }
        }

        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun HomeHeader(hub: HubState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Violet, Cyan))),
                contentAlignment = Alignment.Center,
            ) {
                Text("X", color = Color(0xFF0B0918), fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "XIN HUB",
                    color = TextHi,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.6).sp,
                )
                Text(
                    "skins · effects · lobby",
                    color = TextLow,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Violet.copy(alpha = 0.12f))
                    .border(1.dp, Violet.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    if (hub.refreshing) "SYNC" else "v" + Config.VERSION,
                    color = Violet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
            }
        }
    }
}

@Composable
private fun QuickRow(items: List<Quick>, hub: HubState, onOpen: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (item in items) {
            QuickTile(item, Modifier.weight(1f)) {
                if (item.route == "restore") hub.restoreAll() else onOpen(item.route)
            }
        }
    }
}

@Composable
private fun QuickTile(item: Quick, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(item.tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, contentDescription = null, tint = item.tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                item.title,
                color = TextHi,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(item.sub, color = TextLow, fontSize = 9.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
