package official.nixhub.assistant.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.ui.components.GlassCard
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.Violet

private data class Step(val title: String, val body: String, val image: String?)

private const val GUIDE_BASE = "https://raw.githubusercontent.com/Sparkies01/Images/main/NixHub/AppAssets"

private val STEPS = listOf(
    Step(
        "1 · Enable file access",
        "Open the game folder picker and choose Android/data/com.mobile.legends/files. Grant the persistable permission when Android asks.",
        "$GUIDE_BASE/PermissionGuide/select-mlbb-files-folder.png",
    ),
    Step(
        "2 · Open MLBB settings",
        "Launch Mobile Legends once after installing. The asset folders are created on first boot.",
        "$GUIDE_BASE/FeatureGuide/01-open-settings.png",
    ),
    Step(
        "3 · Manage resources",
        "In the game, open Settings → Manage Resources so the client prepares the asset tree the injector writes into.",
        "$GUIDE_BASE/FeatureGuide/02-manage-resources.png",
    ),
    Step(
        "4 · Run a repair",
        "Use the in-game repair option after a big patch. It rewrites stock files, then the injector can swap them cleanly.",
        "$GUIDE_BASE/FeatureGuide/03-open-repair.png",
    ),
    Step(
        "5 · Verify the folder",
        "The app shows 'Game folder linked' on Home when the tree is writable. If it says missing, pick the folder again.",
        "$GUIDE_BASE/FeatureGuide/05-wait-for-verification.png",
    ),
)

@Composable
fun GuidesScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Panel)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextMid, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Guides",
                    color = TextHi,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.4).sp,
                )
                Text("Get the folder right the first time", color = TextLow, fontSize = 10.5.sp)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(STEPS) { index, step ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(13.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Violet.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Cyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                            Spacer(Modifier.width(9.dp))
                            Text(
                                step.title,
                                color = TextHi,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(step.body, color = TextMid, fontSize = 11.sp, lineHeight = 16.sp)
                        if (step.image != null) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.6f)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                NetImage(step.image, Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x228B5CF6))
                        .padding(13.dp)
                ) {
                    Text(
                        "If a skin refuses to show, restore its backup, run the in-game repair, then apply again.",
                        color = TextLow,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
            item { Spacer(Modifier.height(14.dp)) }
        }
    }
}
