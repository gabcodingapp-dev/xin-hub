package official.nixhub.assistant.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.data.Skin
import official.nixhub.assistant.ui.components.XinBackdrop
import official.nixhub.assistant.ui.components.pressable
import official.nixhub.assistant.ui.screens.EffectsScreen
import official.nixhub.assistant.ui.screens.GuidesScreen
import official.nixhub.assistant.ui.screens.HeroDetailScreen
import official.nixhub.assistant.ui.screens.HeroesScreen
import official.nixhub.assistant.ui.screens.LibraryScreen
import official.nixhub.assistant.ui.screens.LobbyScreen
import official.nixhub.assistant.ui.screens.SettingsScreen
import official.nixhub.assistant.ui.screens.SlotSheet
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Danger
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("home", "Home", Icons.Rounded.GridView),
    Tab("heroes", "Heroes", Icons.Rounded.AutoAwesome),
    Tab("skins", "Skins", Icons.Rounded.Inventory2),
    Tab("settings", "Settings", Icons.Rounded.Settings),
)

@Composable
fun XinRoot() {
    val ctx = LocalContext.current
    val hub = remember { HubState(ctx) }
    val snackbar = remember { SnackbarHostState() }
    var route by rememberSaveable { mutableStateOf("home") }
    var heroSlug by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<Job?>(null) }
    var slotFor by rememberSaveable { mutableStateOf<String?>(null) }
    var upgradeFor by remember { mutableStateOf<Skin?>(null) }
    var showWelcome by rememberSaveable { mutableStateOf(!hub.shizuku.granted) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            hub.connectSaf(uri)
        }
    }

    val zipPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) hub.importArchive(uri)
    }

    DisposableEffect(Unit) {
        val permission = rikka.shizuku.Shizuku.OnRequestPermissionResultListener { _, _ -> hub.onShizukuResult() }
        val binder = rikka.shizuku.Shizuku.OnBinderReceivedListener { hub.refreshShizuku() }
        val dead = rikka.shizuku.Shizuku.OnBinderDeadListener { hub.refreshShizuku() }
        runCatching {
            rikka.shizuku.Shizuku.addRequestPermissionResultListener(permission)
            rikka.shizuku.Shizuku.addBinderReceivedListenerSticky(binder)
            rikka.shizuku.Shizuku.addBinderDeadListener(dead)
        }
        onDispose {
            runCatching {
                rikka.shizuku.Shizuku.removeRequestPermissionResultListener(permission)
                rikka.shizuku.Shizuku.removeBinderReceivedListener(binder)
                rikka.shizuku.Shizuku.removeBinderDeadListener(dead)
            }
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
            Box(modifier = Modifier.navigationBarsPadding()) { SnackbarHost(snackbar) }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Line)
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (tab in TABS) {
                    val active = route == tab.route && heroSlug == null
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .pressable {
                                heroSlug = null
                                upgradeFor = null
                                route = tab.route
                            }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = if (active) Cyan else TextLow,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            tab.label,
                            color = if (active) TextHi else TextLow,
                            fontSize = 9.sp,
                        )
                    }
                }
            }
        },
    ) { padding ->
        XinBackdrop {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .statusBarsPadding(),
            ) {
                val slug = heroSlug
                when {
                    slug != null -> HeroDetailScreen(
                        hub = hub,
                        slug = slug,
                        onBack = { heroSlug = null; upgradeFor = null },
                        onReplace = { slotFor = it },
                        onUpgrade = { upgradeFor = it },
                    )
                    route == "heroes" -> HeroesScreen(hub = hub, onHero = { heroSlug = it; slotFor = null; upgradeFor = null })
                    route == "effects" -> EffectsScreen(hub = hub, onApply = { detail = it })
                    route == "prep" || route == "intros" -> EffectsScreen(hub = hub, onApply = { detail = it }, kind = "prep")
                    route == "lobby" -> LobbyScreen(hub = hub, onApply = { detail = it })
                    route == "library" -> LibraryScreen(hub = hub, onBack = { route = "heroes" })
                    route == "guides" -> GuidesScreen(onBack = { route = "heroes" })
                    else -> SettingsScreen(
                        hub = hub,
                        onPickFolder = { picker.launch(null) },
                        onImport = {
                            zipPicker.launch(
                                arrayOf("application/zip", "application/octet-stream", "application/x-zip-compressed", "*/*")
                            )
                        },
                        onGuides = { route = "guides" },
                    )
                }
            }
        }
    }

    detail?.let { job ->
        JobSheet(hub = hub, job = job, onClose = { detail = null })
    }

    slotFor?.let { slug ->
        SlotSheet(hub = hub, slug = slug, onClose = { slotFor = null })
    }

    upgradeFor?.let { look ->
        SlotSheet(
            hub = hub,
            slug = look.heroSlug,
            onClose = { upgradeFor = null },
            preset = look,
        )
    }

    if (showWelcome) {
        WelcomeAdbOverlay(
            hub = hub,
            onContinue = { showWelcome = false },
        )
    }

    ProgressOverlay(hub = hub)
}

@Composable
private fun WelcomeAdbOverlay(hub: HubState, onContinue: () -> Unit) {
    val granted = hub.shizuku.granted
    val running = hub.shizuku.running
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE080B10))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Panel)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Hello, welcome to XIN HUB",
                color = TextHi,
                fontSize = 19.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Your MLBB companion app. Before continuing, you need to connect to Wireless ADB via Shizuku.",
                color = official.nixhub.assistant.ui.theme.TextMid,
                fontSize = 12.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(official.nixhub.assistant.ui.theme.PanelHi)
                    .clickable { hub.connectShizuku() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (granted) Cyan.copy(alpha = 0.18f) else Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Shizuku",
                        tint = if (granted) Cyan else TextHi,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Shizuku (Wireless ADB)",
                        color = TextHi,
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    )
                    Text(
                        when {
                            granted -> "Connected & permission granted"
                            running -> "Tap to allow XIN HUB in Shizuku"
                            else -> "Tap to connect & request Shizuku permission"
                        },
                        color = if (granted) Cyan else TextLow,
                        fontSize = 11.sp,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (granted) Cyan else official.nixhub.assistant.ui.theme.PanelHi)
                    .clickable { onContinue() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (granted) "Continue to XIN HUB" else "Continue anyway",
                    color = if (granted) official.nixhub.assistant.ui.theme.Void else official.nixhub.assistant.ui.theme.TextMid,
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ProgressOverlay(hub: HubState) {
    val p = hub.progress
    if (!p.running && p.error == null && !p.finished) return

    val failed = p.error != null
    val fraction = if (p.total > 0) (p.done.toFloat() / p.total).coerceIn(0f, 1f) else 0f
    val sweep by animateFloatAsState(fraction * 360f, tween(260))
    val doneScale by animateFloatAsState(if (p.finished && !failed) 1f else 0f, tween(240))
    val loop = rememberInfiniteTransition()
    val spin by loop.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
    )
    val glow by loop.animateFloat(
        0.35f, 0.8f,
        infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6080B10))
            .clickable(enabled = !p.running) { hub.dismissProgress() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Panel)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .size(94.dp)
                        .graphicsLayer { rotationZ = if (p.running) spin else 0f },
                ) {
                    val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(Line, 0f, 360f, false, style = stroke)
                    drawArc(
                        if (failed) Danger else Cyan,
                        0f,
                        if (failed) 360f else sweep,
                        false,
                        style = stroke,
                    )
                    drawCircle(
                        Cyan.copy(alpha = glow),
                        radius = 2.5.dp.toPx(),
                        center = Offset(size.width, size.height / 2f),
                    )
                }
                if (p.finished && !failed) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Cyan)
                            .graphicsLayer { scaleX = doneScale; scaleY = doneScale },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Color(0xFF080B10), modifier = Modifier.size(22.dp))
                    }
                } else {
                    Text("X", color = TextHi, fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                when {
                    failed -> "Injection stopped"
                    p.finished -> "Injection complete"
                    else -> "Injecting skin"
                },
                color = TextHi,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                p.error ?: if (p.finished) "${p.done} files written" else "${p.done} / ${p.total} files",
                color = if (failed) Danger else TextLow,
                fontSize = 11.sp,
            )
            if (p.label.isNotBlank() && p.running) {
                Spacer(Modifier.height(3.dp))
                Text(p.label, color = TextLow, fontSize = 9.5.sp, maxLines = 1)
            }
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Line),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (failed) Danger else Cyan),
                )
            }
        }
    }
}
