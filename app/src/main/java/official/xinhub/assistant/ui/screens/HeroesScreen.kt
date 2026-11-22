package official.xinhub.assistant.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.xinhub.assistant.R
import official.xinhub.assistant.data.Hero
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.EmptyState
import official.xinhub.assistant.ui.components.NetImage
import official.xinhub.assistant.ui.components.SearchField
import official.xinhub.assistant.ui.components.pressable
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.PanelHi
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.XinDisplay

private val ROLES = listOf("All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support")
private val ROLE_ART = mapOf(
    "Tank" to R.drawable.role_1,
    "Fighter" to R.drawable.role_2,
    "Assassin" to R.drawable.role_3,
    "Mage" to R.drawable.role_4,
    "Marksman" to R.drawable.role_5,
    "Support" to R.drawable.role_6,
)
private val HERO_SCRIM = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color(0x99080B10), Color(0xF6080B10)),
)

@Composable
fun HeroesScreen(hub: HubState, onHero: (String) -> Unit) {
    val heroes = hub.catalog.heroes
    var query by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("All") }
    var ascending by rememberSaveable { mutableStateOf(true) }

    val roleCounts = remember(heroes) {
        ROLES.associateWith { name ->
            if (name == "All") heroes.size
            else heroes.count { hero -> hero.roles.any { it.equals(name, true) } }
        }
    }

    val shown by remember(heroes, query, role, ascending) {
        derivedStateOf {
            val filtered = heroes.filter { hero ->
                (role == "All" || hero.roles.any { it.equals(role, true) }) &&
                    (query.isBlank() || hero.name.contains(query, true))
            }
            if (ascending) filtered.sortedBy { it.name }
            else filtered.sortedByDescending { it.name }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "HERO ARCHIVE",
                    color = TextHi,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = XinDisplay,
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "KNOW EVERY CONTENDER",
                    color = TextLow,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Panel)
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(heroes.size.toString(), color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("HEROES", color = TextLow, fontSize = 7.5.sp, letterSpacing = 0.6.sp)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.padding(horizontal = 14.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SearchField(query, "Search ${heroes.size} heroes") { query = it }
            }
            Spacer(Modifier.width(8.dp))
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Panel)
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .pressable { ascending = !ascending }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (ascending) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    if (ascending) "A-Z" else "Z-A",
                    color = TextHi,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("FILTER BY ROLE", color = TextLow, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(
                "${shown.size} SHOWN",
                color = Cyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )
        }
        Spacer(Modifier.height(7.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            items(ROLES, key = { it }) { name ->
                RoleChip(name, roleCounts[name] ?: 0, role == name) { role = name }
            }
        }

        Spacer(Modifier.height(11.dp))
        if (shown.isEmpty()) {
            EmptyState("No hero", "Try another role or search term.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 13.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(shown, key = { it.slug }) { hero -> HeroTile(hero) { onHero(hero.slug) } }
            }
        }
    }
}

@Composable
private fun RoleChip(name: String, count: Int, active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) Cyan else Panel)
            .border(1.dp, if (active) Cyan else Line, RoundedCornerShape(14.dp))
            .pressable(onClick)
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (active) Color(0x33080B10) else PanelHi),
            contentAlignment = Alignment.Center,
        ) {
            if (name == "All") {
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = null,
                    tint = if (active) Cyan else TextMid,
                    modifier = Modifier.size(17.dp),
                )
            } else {
                Image(
                    painter = painterResource(ROLE_ART[name] ?: R.drawable.role_1),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
        Spacer(Modifier.width(7.dp))
        Column {
            Text(
                name,
                color = if (active) Color(0xFF080B10) else TextHi,
                fontSize = 11.5.sp,
                fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                count.toString(),
                color = if (active) Color(0x99080B10) else TextLow,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HeroTile(hero: Hero, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.76f)
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .pressable(onClick),
    ) {
        NetImage(
            url = hero.head,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            targetWidth = 420,
            targetHeight = 560,
        )
        Box(modifier = Modifier.fillMaxSize().background(HERO_SCRIM))

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(9.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xB0080B10))
                .border(1.dp, Cyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 7.dp, vertical = 4.dp),
        ) {
            Text(
                hero.roles.firstOrNull()?.uppercase() ?: "HERO",
                color = Cyan,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 10.dp),
        ) {
            Text(
                hero.name,
                color = TextHi,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hero.heroId > 0) {
                    Text(
                        "ID ${hero.heroId}",
                        color = TextLow,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}
