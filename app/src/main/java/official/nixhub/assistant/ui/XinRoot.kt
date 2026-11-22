package official.nixhub.assistant.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Send
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import official.nixhub.assistant.data.Config
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.data.Skin
import official.nixhub.assistant.ui.components.Pill
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
import official.nixhub.assistant.ui.theme.PanelHi
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("home", "Home", Icons.Rounded.GridView),
    Tab("skins", "Skins", Icons.Rounded.Inventory2),
    Tab("effects", "Effects", Icons.Rounded.AutoAwesome),
    Tab("prep", "Prep", Icons.Rounded.Movie),
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
                    .background(Line.copy(alpha = 0.82f))
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (tab in TABS) {
                    val active = route == tab.route && heroSlug == null
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (active) Cyan.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (active) Cyan.copy(alpha = 0.42f) else Color.Transparent,
                                RoundedCornerShape(13.dp),
                            )
                            .pressable {
                                heroSlug = null
                                upgradeFor = null
                                route = tab.route
                            }
                            .padding(vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 16.dp, height = 2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (active) Cyan else Color.Transparent),
                        )
                        Spacer(Modifier.height(4.dp))
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = if (active) Cyan else TextLow,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            tab.label,
                            color = if (active) TextHi else TextLow,
                            fontSize = 9.sp,
                            fontWeight = if (active) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
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
                    route == "skins" || route == "heroes" -> HeroesScreen(hub = hub, onHero = { heroSlug = it; slotFor = null; upgradeFor = null })
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
    val ctx = LocalContext.current
    val granted = hub.shizuku.granted
    val running = hub.shizuku.running

    fun open(url: String) {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2080B10))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Panel)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Brush.horizontalGradient(listOf(Cyan, Color(0xFF7CE7FF)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF080B10),
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Welcome to XIN HUB",
                color = TextHi,
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "Connect Wireless ADB through Shizuku, then join the channel for updates and help.",
                color = official.nixhub.assistant.ui.theme.TextMid,
                fontSize = 11.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PanelHi)
                    .border(1.dp, if (granted) Cyan.copy(alpha = 0.5f) else Line, RoundedCornerShape(14.dp))
                    .pressable { hub.connectShizuku() }
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (granted) Cyan.copy(alpha = 0.18f) else Line),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = if (granted) Cyan else TextMid,
                        modifier = Modifier.size(21.dp),
                    )
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Connect Shizuku", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            granted -> "Connected · permission granted"
                            running -> "Service on · tap to grant XIN HUB access"
                            else -> "Start Shizuku, then tap to connect"
                        },
                        color = if (granted) Cyan else TextLow,
                        fontSize = 10.sp,
                    )
                }
                Pill(if (granted) "active" else "needed", if (granted) Cyan else Color(0xFFFF6B7A), filled = granted)
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PanelHi)
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                    .pressable { open(Config.TG_CHANNEL_URL) }
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Cyan.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = null, tint = Cyan, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Join Telegram channel", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(Config.TG_CHANNEL, color = Cyan, fontSize = 10.sp)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextLow, modifier = Modifier.size(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PanelHi)
                    .border(1.dp, Line, RoundedCornerShape(14.dp))
                    .pressable { open(Config.TG_OWNER_URL) }
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7CE7FF).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = null, tint = Color(0xFF7CE7FF), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Message owner", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(Config.TG_OWNER, color = Color(0xFF7CE7FF), fontSize = 10.sp)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextLow, modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (granted) Cyan else PanelHi)
                    .pressable(onContinue)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (granted) "Continue to XIN HUB" else "Continue with fallback",
                    color = if (granted) Color(0xFF080B10) else TextMid,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ProgressOverlay(hub: HubState) {
    val p = hub.progress
    if (!p.running && p.error == null && !p.finished) return

    LaunchedEffect(p.finished, p.error) {
        if (p.finished || p.error != null) {
            delay(2100)
            hub.dismissProgress()
        }
    }

    val failed = p.error != null
    val downloading = p.phase == "downloading" && !p.finished && !failed
    val fraction = when {
        downloading && p.bytesTotal > 0 -> (p.bytes.toFloat() / p.bytesTotal).coerceIn(0f, 1f)
        p.total > 0 -> (p.done.toFloat() / p.total).coerceIn(0f, 1f)
        else -> 0f
    }
    val sweep by animateFloatAsState(fraction * 360f, tween(320))
    val doneScale by animateFloatAsState(if (p.finished && !failed) 1f else 0f, tween(260))
    val entry by animateFloatAsState(1f, tween(220))
    val loop = rememberInfiniteTransition()
    val spin by loop.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
    )
    val glow by loop.animateFloat(
        0.3f, 0.85f,
        infiniteRepeatable(tween(680, easing = LinearEasing), RepeatMode.Reverse),
    )
    val shimmer by loop.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(1250, easing = LinearEasing), RepeatMode.Restart),
    )

    fun size(bytes: Long): String = when {
        bytes >= 1024L * 1024 -> "${bytes / 1024 / 1024} MB"
        bytes >= 1024L -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }

    val headline = when {
        failed -> "Injection failed"
        p.finished -> "Injection complete"
        p.phase == "downloading" -> "Downloading skin"
        p.phase == "preparing" -> "Preparing files"
        p.phase == "restoring" -> "Restoring backup"
        else -> "Injecting skin"
    }
    val detail = when {
        failed -> p.error ?: "Unknown error"
        downloading && p.bytesTotal > 0 -> "${size(p.bytes)} / ${size(p.bytesTotal)}"
        downloading -> "${size(p.bytes)} downloaded"
        p.finished -> "${p.done} files written"
        else -> "${p.done} / ${p.total} files"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(entry)
            .background(Color(0xE6080B10))
            .clickable(enabled = !p.running) { hub.dismissProgress() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Panel)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .size(104.dp)
                        .graphicsLayer { rotationZ = if (p.running) spin else 0f },
                ) {
                    val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(Line, 0f, 360f, false, style = stroke)
                    drawArc(
                        if (failed) Danger else Cyan,
                        0f,
                        when {
                            failed -> 360f
                            downloading && p.bytesTotal == 0L -> 92f
                            else -> sweep
                        },
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Cyan)
                            .graphicsLayer { scaleX = doneScale; scaleY = doneScale },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFF080B10),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    Text(
                        "X",
                        color = if (failed) Danger else TextHi,
                        fontSize = 23.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(17.dp))
            Text(
                headline,
                color = TextHi,
                fontSize = 17.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                detail,
                color = if (failed) Danger else TextLow,
                fontSize = 11.sp,
            )
            if (p.label.isNotBlank() && !p.finished && !failed) {
                Spacer(Modifier.height(4.dp))
                Text(p.label, color = Cyan.copy(alpha = 0.75f), fontSize = 9.5.sp, maxLines = 1)
            }
            Spacer(Modifier.height(15.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.74f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Line),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (failed) Danger else Cyan),
                )
                if (p.running) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.28f)
                            .height(4.dp)
                            .graphicsLayer { translationX = shimmer * 260f }
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.16f)),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (failed) "Tap anywhere to close" else if (p.finished) "Done" else "Keep the app open",
                color = TextLow,
                fontSize = 9.sp,
            )
        }
    }
}
