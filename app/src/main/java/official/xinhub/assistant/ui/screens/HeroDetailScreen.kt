package official.xinhub.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.Skin
import official.xinhub.assistant.data.toJob
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.EmptyState
import official.xinhub.assistant.ui.components.GhostAction
import official.xinhub.assistant.ui.components.MenuThumb
import official.xinhub.assistant.ui.components.Pill
import official.xinhub.assistant.ui.components.PrimaryAction
import official.xinhub.assistant.ui.components.SearchField
import official.xinhub.assistant.ui.components.ScreenHeader
import official.xinhub.assistant.ui.components.SegmentedTabs
import official.xinhub.assistant.ui.components.pressable
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.PanelHi
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.Void
import official.xinhub.assistant.ui.theme.XinDisplay
import official.xinhub.assistant.ui.theme.tierColor

private val GROUPS = listOf("visual", "upgrade", "custom")
private val GROUP_LABEL = mapOf("visual" to "Visual", "upgrade" to "Upgrade", "custom" to "Custom")

@Composable
fun HeroDetailScreen(
    hub: HubState,
    slug: String,
    onBack: () -> Unit,
    onReplace: (String) -> Unit,
    onUpgrade: (Skin) -> Unit,
    onUpgradeCenter: () -> Unit,
) {
    val hero = remember(slug, hub.catalog.heroes) {
        hub.catalog.heroes.firstOrNull { it.slug == slug }
    }
    val all = remember(slug, hub.catalog.skins) {
        hub.catalog.skins.filter { it.heroSlug == slug }
    }
    var tab by rememberSaveable(slug) { mutableStateOf("visual") }
    var query by rememberSaveable(slug) { mutableStateOf("") }

    val shown by remember(all, tab, query) {
        derivedStateOf {
            val pool = if (tab == "upgrade") all.filter { it.injectable }
                       else all.filter { it.group == tab }
            pool.filter { query.isBlank() || it.name.contains(query, true) }
        }
    }
    val pager = rememberPagerState(pageCount = { shown.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(shown, tab, query) {
        if (shown.isNotEmpty()) {
            pager.scrollToPage(pager.currentPage.coerceAtMost(shown.lastIndex))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = hero?.name ?: slug,
            subtitle = hero?.roles?.joinToString(" · ")?.ifBlank { "hero" } ?: "hero",
            onBack = onBack,
        ) {
            Pill("${all.size} looks", Cyan)
        }

        Spacer(Modifier.height(8.dp))
        SegmentedTabs(
            items = GROUPS.map { group ->
                val count = if (group == "upgrade") all.count { it.injectable }
                            else all.count { it.group == group }
                Triple(group, GROUP_LABEL[group] ?: group, count)
            },
            selected = tab,
            onSelect = { tab = it; query = "" },
        )

        Spacer(Modifier.height(9.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(PanelHi, Panel)))
                .border(1.dp, Line, RoundedCornerShape(14.dp)),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Cyan.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (tab == "upgrade") Icons.Rounded.AutoAwesome else Icons.Rounded.SwapHoriz,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (tab == "upgrade") "Universal upgrade" else "Replace a skin",
                        color = TextHi,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "${shown.size} available · tap a look, then pick its slot",
                        color = TextLow,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                PrimaryAction(if (tab == "upgrade") "UPGRADE" else "SLOTS", Modifier.width(84.dp)) {
                    onReplace(slug)
                }
            }
        }

        if (tab == "upgrade") {
            Spacer(Modifier.height(7.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Panel)
                    .border(1.dp, Cyan.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 11.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Every injectable skin can be used as an upgrade look.",
                    color = TextMid,
                    fontSize = 9.5.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                GhostAction("CENTER") { onUpgradeCenter() }
            }
        }

        Spacer(Modifier.height(9.dp))
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            SearchField(query, "Search ${GROUP_LABEL[tab]} skins") { query = it }
        }
        Spacer(Modifier.height(8.dp))

        if (shown.isEmpty()) {
            EmptyState("No skins here", "This hero has no ${GROUP_LABEL[tab]} entries yet.")
        } else {
            val current = if (pager.currentPage in shown.indices) pager.currentPage else 0
            HorizontalPager(
                state = pager,
                contentPadding = PaddingValues(horizontal = 14.dp),
                pageSpacing = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(244.dp),
            ) { page ->
                SkinStageCard(shown[page], tab == "upgrade") {
                    if (tab == "upgrade") {
                        onUpgrade(shown[page])
                    } else {
                        val job = shown[page].toJob()
                        if (job != null) hub.apply(job) else hub.say("${shown[page].name} has no script yet")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PagerArrow(Icons.Rounded.ChevronLeft) {
                    scope.launch {
                        pager.animateScrollToPage(if (current == 0) shown.lastIndex else current - 1)
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "${current + 1} / ${shown.size}",
                        color = TextHi,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("SWIPE", color = TextLow, fontSize = 8.sp, letterSpacing = 1.sp)
                }
                PagerArrow(Icons.Rounded.ChevronRight) {
                    scope.launch {
                        pager.animateScrollToPage(if (current == shown.lastIndex) 0 else current + 1)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val windowStart = (current - 5).coerceAtLeast(0)
            val windowEnd = (windowStart + 11).coerceAtMost(shown.size)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                items(count = windowEnd - windowStart, key = { windowStart + it }) { offset ->
                    val index = windowStart + offset
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 62.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (index == current) Cyan.copy(alpha = 0.16f) else Panel)
                            .border(
                                1.5.dp,
                                if (index == current) Cyan else Line,
                                RoundedCornerShape(9.dp),
                            )
                            .pressable {
                                scope.launch { pager.animateScrollToPage(index) }
                            }
                            .padding(3.dp),
                    ) {
                        MenuThumb(
                            shown[index].image,
                            Modifier.fillMaxSize(),
                            ContentScale.Fit,
                            240,
                            300,
                            7.dp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun PagerArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(50))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(50))
            .pressable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = TextMid, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun SkinStageCard(skin: Skin, upgradeMode: Boolean, onClick: () -> Unit) {
    val applied = Xin.prefs.isApplied(skin.id)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(PanelHi, Panel, Void)))
            .border(1.dp, if (upgradeMode) Cyan.copy(alpha = 0.45f) else Line, RoundedCornerShape(16.dp))
            .pressable(onClick),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.44f)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x99FFFFFF)),
            contentAlignment = Alignment.Center,
        ) {
            MenuThumb(
                skin.splash ?: skin.image,
                Modifier.fillMaxSize().padding(4.dp),
                ContentScale.Fit,
                456,
                534,
                10.dp,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.53f)
                .padding(end = 12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Pill(skin.tier.ifBlank { "skin" }, tierColor(skin.tier))
                if (upgradeMode || skin.group == "upgrade") Pill("upgrade", Cyan)
                if (skin.group == "custom") Pill("custom", Cyan)
                if (applied) Pill("on", Cyan, filled = true)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                skin.name,
                color = TextHi,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = XinDisplay,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(skin.hero, color = TextLow, fontSize = 10.sp, maxLines = 1)
            Spacer(Modifier.height(10.dp))
            PrimaryAction(
                text = if (upgradeMode) "UPGRADE THIS" else if (skin.injectable) "APPLY SKIN" else "NO SCRIPT",
                enabled = upgradeMode || skin.injectable,
                icon = if (upgradeMode) Icons.Rounded.AutoAwesome else Icons.Rounded.SwapHoriz,
                onClick = onClick,
            )
        }
    }
}
