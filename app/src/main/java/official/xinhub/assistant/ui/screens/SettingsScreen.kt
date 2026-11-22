package official.xinhub.assistant.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.Config
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.ui.HubState
import official.xinhub.assistant.ui.components.GlassCard
import official.xinhub.assistant.ui.components.Pill
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Danger
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.XinDisplay

@Composable
fun SettingsScreen(
    hub: HubState,
    onPickFolder: () -> Unit,
    onImport: () -> Unit = {},
    onGuides: () -> Unit = {},
    onUpgrade: () -> Unit = {},
) {
    val ctx = LocalContext.current
    var rootOn by remember { mutableStateOf(Xin.prefs.useRoot) }
    val shizuku = hub.shizuku

    fun open(url: String) {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        item {
            Text("SETUP", color = TextHi, fontSize = 23.sp, fontWeight = FontWeight.Black, fontFamily = XinDisplay, letterSpacing = 1.sp)
            Text("Shizuku is the main permission. Everything else is a fallback.", color = TextLow, fontSize = 10.5.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Cyan.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Cyan, modifier = Modifier.size(17.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shizuku", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    !shizuku.installed -> "not installed"
                                    shizuku.granted -> "connected · main permission"
                                    shizuku.running -> "service on · tap to grant"
                                    else -> "service stopped · start it"
                                },
                                color = TextLow,
                                fontSize = 10.sp,
                            )
                        }
                        Pill(
                            if (shizuku.granted) "active" else if (shizuku.installed) "idle" else "missing",
                            if (shizuku.granted) Cyan else if (shizuku.installed) TextMid else Danger,
                            filled = shizuku.granted,
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    ActionRow(
                        if (shizuku.granted) "Reconnect" else "Grant access",
                        "Recommended · no root, no superuser prompt",
                        Icons.Rounded.Security,
                    ) { hub.connectShizuku() }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(13.dp)) {
                    Text("Community", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Updates, fixes and direct help.", color = TextLow, fontSize = 10.sp)
                    Spacer(Modifier.height(9.dp))
                    ActionRow("Join Telegram channel", Config.TG_CHANNEL, Icons.Rounded.Send) {
                        open(Config.TG_CHANNEL_URL)
                    }
                    Spacer(Modifier.height(7.dp))
                    ActionRow("Message owner", Config.TG_OWNER, Icons.Rounded.Send) {
                        open(Config.TG_OWNER_URL)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(13.dp)) {
                    ActionRow("Import a skin zip", "Custom skins from any file manager", Icons.Rounded.FolderOpen) { onImport() }
                    Spacer(Modifier.height(7.dp))
                    ActionRow("Pick game folder", "SAF fallback if Shizuku is unavailable", Icons.Rounded.FolderOpen) { onPickFolder() }
                    Spacer(Modifier.height(9.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Root fallback", color = TextHi, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Requires su, only if the other two fail", color = TextLow, fontSize = 9.5.sp)
                        }
                        Switch(
                            checked = rootOn,
                            onCheckedChange = { on ->
                                rootOn = on
                                if (on) hub.connectRoot() else hub.disconnect()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF090A0E),
                                checkedTrackColor = Cyan,
                            ),
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(13.dp)) {
                    ActionRow("Refresh catalog", "Pull the newest skins and effects", Icons.Rounded.Refresh) { hub.refresh() }
                    Spacer(Modifier.height(7.dp))
                    ActionRow("Upgrade skins", "Second Forms and free upgrades", Icons.Rounded.AutoAwesome) { onUpgrade() }
                    Spacer(Modifier.height(7.dp))
                    ActionRow("Enable protection", "Stop the client wiping injected files", Icons.Rounded.Security) {
                        hub.apply(
                            Job(
                                id = "restore-protection-on",
                                kind = Job.Kind.EFFECT,
                                title = "Restoration protection",
                                subtitle = "protection",
                                script = Config.PROTECT_ON,
                                image = null,
                            )
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    ActionRow("Disable protection", "Back to stock restore behaviour", Icons.Rounded.Restore) {
                        hub.apply(
                            Job(
                                id = "restore-protection-off",
                                kind = Job.Kind.EFFECT,
                                title = "Stock restore",
                                subtitle = "protection",
                                script = Config.PROTECT_OFF,
                                image = null,
                            )
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    ActionRow("Clear image cache", "Frees app storage", Icons.Rounded.Restore) {
                        val freed = Xin.injector.clearCache()
                        hub.say(if (freed > 0) "Cleared $freed cached archives" else "Cache already empty")
                    }
                    Spacer(Modifier.height(7.dp))
                    ActionRow("Setup guide", "Permissions and first launch", Icons.Rounded.LibraryBooks) { onGuides() }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(13.dp)) {
                    Text("XIN HUB", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("v${Config.VERSION} · ${Config.PACKAGE}", color = TextLow, fontSize = 9.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Line))
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("Developed by ", color = TextLow, fontSize = 10.5.sp)
                        Text(Config.DEV, color = TextHi, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TextMid, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextHi, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextLow, fontSize = 9.sp, maxLines = 1)
        }
    }
}
