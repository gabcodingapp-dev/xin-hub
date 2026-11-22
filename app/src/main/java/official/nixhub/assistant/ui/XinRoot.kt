package official.nixhub.assistant.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.ui.components.XinBackdrop
import official.nixhub.assistant.ui.screens.EffectsScreen
import official.nixhub.assistant.ui.screens.GuidesScreen
import official.nixhub.assistant.ui.screens.HomeScreen
import official.nixhub.assistant.ui.screens.LibraryScreen
import official.nixhub.assistant.ui.screens.LobbyScreen
import official.nixhub.assistant.ui.screens.SettingsScreen
import official.nixhub.assistant.ui.screens.SkinsScreen
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.Violet

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("home", "Home", Icons.Rounded.GridView),
    Tab("skins", "Skins", Icons.Rounded.Style),
    Tab("effects", "Effects", Icons.Rounded.AutoAwesome),
    Tab("lobby", "Lobby", Icons.Rounded.MusicNote),
    Tab("settings", "Setup", Icons.Rounded.Settings),
)

@Composable
fun XinRoot() {
    val ctx = LocalContext.current
    val hub = remember { HubState(ctx) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var route by rememberSaveable { mutableStateOf("home") }
    var detail by remember { mutableStateOf<Job?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (t: Throwable) {
                // Some providers refuse persistable grants; the session grant still works.
            }
            hub.connectSaf(uri)
        }
    }

    LaunchedEffect(Unit) { hub.boot() }

    LaunchedEffect(hub.message) {
        val msg = hub.message ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        hub.message = null
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            Box(modifier = Modifier.navigationBarsPadding()) {
                SnackbarHost(snackbar)
            }
        },
        bottomBar = {
            XinBottomBar(
                current = route,
                onSelect = { route = it },
            )
        },
    ) { padding ->
        XinBackdrop {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .statusBarsPadding()
            ) {
                when (route) {
                    "home" -> HomeScreen(
                        hub = hub,
                        onOpen = { route = it },
                        onApply = { detail = it },
                        onPickFolder = { picker.launch(null) },
                    )
                    "skins" -> SkinsScreen(hub = hub, onApply = { detail = it })
                    "effects" -> EffectsScreen(hub = hub, onApply = { detail = it })
                    "intros" -> EffectsScreen(hub = hub, onApply = { detail = it }, kind = "intro")
                    "lobby" -> LobbyScreen(hub = hub, onApply = { detail = it })
                    "library" -> LibraryScreen(hub = hub, onBack = { route = "home" })
                    "guides" -> GuidesScreen(onBack = { route = "home" })
                    else -> SettingsScreen(
                        hub = hub,
                        onPickFolder = { picker.launch(null) },
                    )
                }
            }
        }
    }

    detail?.let { job ->
        JobSheet(
            hub = hub,
            job = job,
            onClose = { detail = null },
        )
    }

    ProgressOverlay(hub = hub)
}

@Composable
private fun XinBottomBar(current: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xF20B0918), Color(0xFF07060F))))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (tab in TABS) {
            val active = current == tab.route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(tab.route) }
                    .padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 26.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (active) Violet.copy(alpha = 0.16f) else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (active) Cyan else TextLow,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    tab.label,
                    color = if (active) TextHi else TextLow,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.2.sp,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(width = 12.dp, height = 2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (active) Cyan else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun ProgressOverlay(hub: HubState) {
    val p = hub.progress
    if (!p.running && p.error == null && !p.finished) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC05040C))
            .clickable(enabled = !p.running) { hub.dismissProgress() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Panel)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                when {
                    p.error != null -> "Stopped"
                    p.finished -> "Done"
                    else -> "Working"
                },
                color = TextHi,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                p.error ?: "${p.done} / ${p.total} files",
                color = TextLow,
                fontSize = 12.sp,
            )
            if (p.label.isNotBlank() && p.running) {
                Spacer(Modifier.height(4.dp))
                Text(p.label, color = TextLow, fontSize = 10.sp)
            }
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Line)
            ) {
                val fraction = if (p.total > 0) (p.done.toFloat() / p.total).coerceIn(0f, 1f) else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Violet, Cyan)))
                )
            }
        }
    }
}
