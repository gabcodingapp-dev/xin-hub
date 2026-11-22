package official.xinhub.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.xinhub.assistant.Xin
import official.xinhub.assistant.data.GameControl
import official.xinhub.assistant.data.Job
import official.xinhub.assistant.ui.components.NetImage
import official.xinhub.assistant.ui.components.Pill
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Danger
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.Violet
import official.xinhub.assistant.ui.theme.tierColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSheet(hub: HubState, job: Job, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val applied = Xin.prefs.isApplied(job.id)
    val backups = Xin.injector.backupCount(job.id)
    val ctx = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Panel,
        contentColor = TextHi,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Line)
                    .padding(horizontal = 22.dp, vertical = 2.dp)
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 22.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.9f)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                NetImage(url = job.image, modifier = Modifier.fillMaxWidth().aspectRatio(1.9f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color(0x00000000), Color(0xE60C0A18)))
                        )
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        job.title,
                        color = TextHi,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.3).sp,
                    )
                    Text(job.subtitle, color = TextMid, fontSize = 12.sp)
                }
                Pill(
                    text = if (applied) "applied" else "ready",
                    color = if (applied) Cyan else Violet,
                    filled = true,
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(text = job.kind.name.lowercase(), color = tierColor(job.kind.name))
                if (backups > 0) Pill(text = "$backups backups", color = TextLow)
                Pill(text = "safe swap", color = Cyan)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                when (job.kind) {
                    Job.Kind.SKIN -> "Copies the skin script into the game asset tree and backs up every file it touches."
                    Job.Kind.EFFECT -> "Replaces the matching effect files. Original files are stored before the swap."
                    Job.Kind.LOBBY -> "Drops the lobby track into the game's sound asset folder."
                },
                color = TextLow,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    hub.apply(job)
                    onClose()
                },
                enabled = !hub.progress.running,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Violet, contentColor = TextHi),
            ) {
                Text(if (applied) "Re-apply" else "Apply to game", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    hub.restore(job.id)
                    onClose()
                },
                enabled = backups > 0 && !hub.progress.running,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    if (backups > 0) "Restore original" else "No backup yet",
                    color = if (backups > 0) TextHi else Danger,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    GameControl.launch(ctx)
                    onClose()
                },
                enabled = !hub.progress.running,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Open MLBB", color = Cyan, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
