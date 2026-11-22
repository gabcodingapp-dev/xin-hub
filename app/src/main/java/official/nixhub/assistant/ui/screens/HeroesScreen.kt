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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.data.Hero
import official.nixhub.assistant.ui.HubState
import official.nixhub.assistant.ui.components.EmptyState
import official.nixhub.assistant.ui.components.FilterChip
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.components.pressable
import official.nixhub.assistant.ui.components.SearchField
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.XinDisplay

private val ROLES = listOf("All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support")
private val HERO_SCRIM = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color(0x66080B10), Color(0xEE080B10)),
)

@Composable
fun HeroesScreen(hub: HubState, onHero: (String) -> Unit) {
    val heroes = hub.catalog.heroes
    var query by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("All") }
    var ascending by rememberSaveable { mutableStateOf(true) }

    val roleCounts = remember(heroes) {
        ROLES.associateWith { role ->
            if (role == "All") heroes.size
            else heroes.count { hero -> hero.roles.any { it.equals(role, true) } }
        }
    }

    val shown by remember(heroes, query, role, ascending) {
        derivedStateOf {
            val filtered = heroes.filter { hero ->
                (role == "All" || hero.roles.any { it.equals(role, true) }) &&
                    (query.isBlank() || hero.name.contains(query, true))
            }
            if (ascending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 14.dp).padding(top = 10.dp)) {
            Text(
                "HERO ARCHIVE",
                color = TextHi,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = XinDisplay,
                letterSpacing = 1.sp,
            )
            Text("KNOW EVERY CONTENDER", color = TextLow, fontSize = 9.5.sp, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    SearchField(query, "Search ${heroes.size} heroes") { query = it }
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Panel)
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .pressable { ascending = !ascending }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (ascending) "A-Z" else "Z-A",
                        color = if (ascending) TextHi else Cyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Pick a role", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("${shown.size} HEROES", color = TextLow, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp)
        }
        Spacer(Modifier.height(6.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(ROLES, key = { it }) { name ->
                FilterChip("$name · ${roleCounts[name] ?: 0}", role == name) { role = name }
            }
        }

        Spacer(Modifier.height(10.dp))
        if (shown.isEmpty()) {
            EmptyState("No hero", "Try another role or search term.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shown, key = { it.slug }) { hero -> HeroTile(hero) { onHero(hero.slug) } }
            }
        }
    }
}

@Composable
private fun HeroTile(hero: Hero, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(10.dp))
            .background(Panel)
            .pressable(onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PanelHi),
            contentAlignment = Alignment.Center,
        ) {
            NetImage(
                url = hero.head,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                targetWidth = 456,
                targetHeight = 534,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color(0xCC080B10))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            Text(
                hero.name,
                color = TextHi,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                hero.roles.firstOrNull()?.uppercase() ?: "HERO",
                color = Cyan,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}
