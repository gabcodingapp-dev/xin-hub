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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import official.nixhub.assistant.ui.HubState
import official.nixhub.assistant.ui.components.EmptyState
import official.nixhub.assistant.ui.components.GlassCard
import official.nixhub.assistant.ui.components.Pill
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Danger
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.Violet
import official.nixhub.assistant.ui.theme.XinDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(hub: HubState, onBack: () -> Unit) {
    val applied = Xin.prefs.applied.value
    val fmt = SimpleDateFormat("dd MMM · HH:mm", Locale.US)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "My Library",
                    color = TextHi,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = XinDisplay,
                    letterSpacing = (-0.4).sp,
                )
                Text("${applied.size} applied · backups kept on device", color = TextLow, fontSize = 10.5.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Danger.copy(alpha = 0.12f))
                    .border(1.dp, Danger.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .clickable { hub.restoreAll() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Restore, contentDescription = null, tint = Danger, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Restore all", color = Danger, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Violet, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Storage", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        hub.storage?.label ?: "not connected",
                        color = TextLow,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Pill(if (hub.storage != null) "linked" else "offline", if (hub.storage != null) Cyan else Danger, filled = true)
            }
        }

        if (applied.isEmpty()) {
            EmptyState("Nothing applied yet", "Anything you apply lands here with a one-tap restore.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(applied.sortedByDescending { it.at }, key = { it.jobId }) { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Panel)
                            .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Brush.linearGradient(listOf(Violet.copy(alpha = 0.5f), Cyan.copy(alpha = 0.4f)))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                record.kind.take(1),
                                color = Color(0xFF07060F),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                record.title,
                                color = TextHi,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${record.subtitle} · ${record.files} files · ${fmt.format(Date(record.at))}",
                                color = TextLow,
                                fontSize = 9.5.sp,
                                maxLines = 1,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Cyan.copy(alpha = 0.12f))
                                .clickable { hub.restore(record.jobId) }
                                .padding(horizontal = 11.dp, vertical = 7.dp)
                        ) {
                            Text("Restore", color = Cyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
