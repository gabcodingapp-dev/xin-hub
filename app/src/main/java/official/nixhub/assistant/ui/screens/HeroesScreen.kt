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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.data.Hero
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

private val ROLES = listOf("All", "Fighter", "Mage", "Assassin", "Marksman", "Support", "Tank")

@Composable
fun HeroesScreen(hub: HubState, onHero: (String) -> Unit) {
    val heroes = hub.catalog.heroes
    var query by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("All") }

    val shown by remember(heroes, query, role) {
        derivedStateOf {
            heroes.filter { hero ->
                (role == "All" || hero.roles.any { it.equals(role, true) }) &&
                    (query.isBlank() || hero.name.contains(query, true))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 14.dp).padding(top = 10.dp)) {
            Text("HEROES", color = TextHi, fontSize = 23.sp, fontWeight = FontWeight.Black, fontFamily = XinDisplay, letterSpacing = 1.sp)
            Text("${heroes.size} heroes · pick one to change its skins", color = TextLow, fontSize = 10.5.sp)
            Spacer(Modifier.height(9.dp))
            SearchField(query, "Search hero") { query = it }
        }

        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(ROLES, key = { it }) { name ->
                FilterChip(name, role == name) { role = name }
            }
        }

        Spacer(Modifier.height(10.dp))
        if (shown.isEmpty()) {
            EmptyState("No hero", "Try another role or search term.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shown, key = { it.slug }) { hero -> HeroTile(hero) { onHero(hero.slug) } }
            }
        }
    }
}

@Composable
private fun HeroTile(hero: Hero, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .clickable { onClick() },
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.92f)) {
            NetImage(hero.head, Modifier.fillMaxSize())
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
            Text(
                hero.name,
                color = TextHi,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                hero.roles.joinToString(" · "),
                color = Cyan,
                fontSize = 8.5.sp,
                maxLines = 1,
            )
        }
    }
}
