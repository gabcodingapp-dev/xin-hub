package official.nixhub.assistant.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import official.nixhub.assistant.data.Job
import official.nixhub.assistant.data.LobbySound
import official.nixhub.assistant.data.toJob
import official.nixhub.assistant.ui.HubState
import official.nixhub.assistant.ui.components.EmptyState
import official.nixhub.assistant.ui.components.NetImage
import official.nixhub.assistant.ui.components.Pill
import official.nixhub.assistant.ui.theme.Amber
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.Violet
import official.nixhub.assistant.ui.theme.XinDisplay

@Composable
fun LobbyScreen(hub: HubState, onApply: (Job) -> Unit) {
    val sounds = hub.catalog.lobby
    var playing by remember { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { player?.release() }
            player = null
        }
    }

    fun toggle(sound: LobbySound) {
        val url = sound.preview ?: return
        if (playing == sound.title) {
            runCatching { player?.stop() }
            runCatching { player?.release() }
            player = null
            playing = null
            return
        }
        runCatching { player?.release() }
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        runCatching {
            mp.setDataSource(url)
            mp.setOnPreparedListener { it.start() }
            mp.setOnCompletionListener {
                playing = null
                runCatching { it.release() }
                player = null
            }
            mp.prepareAsync()
        }.onFailure {
            runCatching { mp.release() }
            hub.say("Preview failed")
            return
        }
        player = mp
        playing = sound.title
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp)) {
            Text(
                "Lobby Sounds",
                color = TextHi,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = XinDisplay,
                letterSpacing = (-0.6).sp,
            )
            Text("${sounds.size} tracks · tap to preview", color = TextLow, fontSize = 11.sp)
        }

        if (sounds.isEmpty()) {
            EmptyState("No tracks yet", "The catalog refresh will pull the lobby list.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(sounds, key = { it.title }) { sound ->
                    val applied = Xin.prefs.isApplied(
                        "lobby-" + sound.title.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Panel)
                            .border(1.dp, Line.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            NetImage(sound.image, Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                sound.title,
                                color = TextHi,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(sound.artist, color = TextLow, fontSize = 10.5.sp, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                if (applied) Pill("applied", Cyan, filled = true)
                                Pill("lobby bgm", Amber)
                            }
                        }
                        if (sound.preview != null) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Violet.copy(alpha = 0.16f))
                                    .clickable { toggle(sound) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    if (playing == sound.title) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Violet,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(listOf(Violet, Cyan))
                                )
                                .clickable {
                                    val job = sound.toJob()
                                    if (job != null) onApply(job) else hub.say("No script for this track")
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Apply", color = Color(0xFF07060F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
