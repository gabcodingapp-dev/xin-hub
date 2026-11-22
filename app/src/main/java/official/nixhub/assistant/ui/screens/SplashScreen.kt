package official.nixhub.assistant.ui.screens

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import official.nixhub.assistant.ui.theme.Cyan
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.Violet
import official.nixhub.assistant.ui.theme.Void
import official.nixhub.assistant.ui.theme.XinDisplay

/** Startup card. Runs for one beat while the catalog snapshot loads underneath. */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val enter by animateFloatAsState(if (started) 1f else 0f, tween(420, easing = FastOutSlowInEasing))
    val sweep by animateFloatAsState(if (started) 1f else 0f, tween(1200, easing = FastOutSlowInEasing))
    val loop = rememberInfiniteTransition()
    val spin by loop.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
    )
    val pulse by loop.animateFloat(
        0.72f, 1f,
        infiniteRepeatable(tween(760, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    )

    LaunchedEffect(Unit) {
        started = true
        delay(1350)
        onDone()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Void),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(enter).padding(24.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .size(104.dp)
                        .graphicsLayer { rotationZ = spin * 360f },
                ) {
                    val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(Violet, 0f, 86f, false, style = stroke)
                    drawArc(Cyan, 180f, 86f, false, style = stroke)
                    drawArc(Color(0x223DDC97), 90f, 180f, false, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(Cyan, radius = 3.dp.toPx(), center = Offset(size.width, size.height / 2f))
                }
                Text(
                    "X",
                    color = TextHi,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = XinDisplay,
                    modifier = Modifier.scale(pulse),
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "XIN HUB",
                color = TextHi,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = XinDisplay,
                letterSpacing = 3.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text("SKIN · EFFECT · PREP", color = TextLow, fontSize = 9.sp, letterSpacing = 2.sp)

            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Line),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(sweep)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Violet, Cyan))),
                )
            }
            Spacer(Modifier.height(9.dp))
            Text("preparing the bench", color = TextLow, fontSize = 9.sp)
        }
    }
}
