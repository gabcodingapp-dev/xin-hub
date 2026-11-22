package official.nixhub.assistant.ui.screens

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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
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
import official.nixhub.assistant.Xin
import official.nixhub.assistant.data.Config
import official.nixhub.assistant.ui.HubState
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

@Composable
fun SettingsScreen(
    hub: HubState,
    onPickFolder: () -> Unit,
    onImport: () -> Unit = {},
    onGuides: () -> Unit = {},
) {
    val ctx = LocalContext.current
    var rootOn by remember { mutableStateOf(Xin.prefs.useRoot) }
    val shizuku = hub.shizuku

    fun open(url: String) {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Setup",
                color = TextHi,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.6).sp,
            )
            Text("Connect the game, keep the catalog fresh", color = TextLow, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Violet.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = Violet, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Game folder", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                hub.storage?.label ?: "Android/data/com.mobile.legends/files",
                                color = TextLow,
                                fontSize = 10.sp,
                            )
                        }
                        Pill(if (hub.storage != null) "linked" else "missing", if (hub.storage != null) Cyan else Danger, filled = true)
                    }
                    Spacer(Modifier.height(12.dp))
                    ActionRow("Pick MLBB files folder", "SAF tree · no root needed", Icons.Rounded.FolderOpen) {
                        onPickFolder()
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Root mode", color = TextHi, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                            Text("su shell writes straight into the game", color = TextLow, fontSize = 10.sp)
                        }
                        Switch(
                            checked = rootOn,
                            onCheckedChange = { on ->
                                rootOn = on
                                if (on) hub.connectRoot() else hub.disconnect()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF07060F),
                                checkedTrackColor = Cyan,
                            ),
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Cyan.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Shizuku", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    !shizuku.installed -> "not installed"
                                    shizuku.granted -> "granted · adb shell access"
                                    shizuku.running -> "running · needs permission"
                                    else -> "installed · service stopped"
                                },
                                color = TextLow,
                                fontSize = 10.sp,
                            )
                        }
                        Pill(
                            if (shizuku.granted) "ready" else if (shizuku.running) "idle" else "off",
                            if (shizuku.granted) Cyan else if (shizuku.running) Violet else Danger,
                            filled = true,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Same write power as root without the superuser prompt. Start Shizuku, then grant access here.",
                        color = TextLow,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                    Spacer(Modifier.height(11.dp))
                    ActionRow(
                        if (shizuku.granted) "Reconnect Shizuku" else "Grant Shizuku access",
                        if (shizuku.installed) "Tap after starting the service" else "Install Shizuku from the store first",
                        Icons.Rounded.Security,
                    ) {
                        hub.connectShizuku()
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Violet.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.LibraryBooks, contentDescription = null, tint = Violet, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Custom skin", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Import a zip and inject it", color = TextLow, fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Accepts any archive with dragon2017/assets inside, or bare Art / Document / Interface folders. Originals are backed up first.",
                        color = TextLow,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                    Spacer(Modifier.height(11.dp))
                    ActionRow("Pick a skin zip", "From Downloads or any file manager", Icons.Rounded.FolderOpen) {
                        onImport()
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    ActionRow("Refresh catalog", "Pull the newest skins and effects", Icons.Rounded.Refresh) {
                        hub.refresh()
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionRow("Restore all backups", "Put stock files back before an update", Icons.Rounded.Restore) {
                        hub.restoreAll()
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionRow("Clear cached scripts", "Frees space in app storage", Icons.Rounded.Bolt) {
                        val freed = Xin.injector.clearCache()
                        hub.say(if (freed > 0) "Cleared $freed cached archives" else "Cache already empty")
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionRow("Enable restoration protection", "Blocks the client from wiping injected files", Icons.Rounded.Security) {
                        hub.apply(
                            official.nixhub.assistant.data.Job(
                                id = "restore-protection-on",
                                kind = official.nixhub.assistant.data.Job.Kind.EFFECT,
                                title = "Restoration protection",
                                subtitle = "protection",
                                script = Config.PROTECT_ON,
                                image = null,
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionRow("Disable restoration protection", "Put the stock behaviour back", Icons.Rounded.Restore) {
                        hub.apply(
                            official.nixhub.assistant.data.Job(
                                id = "restore-protection-off",
                                kind = official.nixhub.assistant.data.Job.Kind.EFFECT,
                                title = "Stock restore",
                                subtitle = "protection",
                                script = Config.PROTECT_OFF,
                                image = null,
                            )
                        )
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    ActionRow("Setup guide", "Step by step with screenshots", Icons.Rounded.LibraryBooks) {
                        onGuides()
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionRow("Project source", Config.PROJECT_URL, Icons.Rounded.Info) {
                        open(Config.PROJECT_URL)
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionRow("Asset catalog", Config.ASSETS_URL, Icons.Rounded.Security) {
                        open(Config.ASSETS_URL)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("XIN HUB", color = TextHi, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            Text("v${Config.VERSION} · ${Config.PACKAGE}", color = TextLow, fontSize = 10.sp)
                        }
                        Pill("official", Cyan, filled = true)
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Line.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Developed by ", color = TextLow, fontSize = 11.sp)
                        Text(Config.DEV, color = TextHi, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Skins, effects and lobby assets are client-side only. Nothing is sent to a server.",
                        color = TextLow,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(1.dp, Line.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TextMid, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextHi, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextLow, fontSize = 9.5.sp, maxLines = 1)
        }
    }
}
