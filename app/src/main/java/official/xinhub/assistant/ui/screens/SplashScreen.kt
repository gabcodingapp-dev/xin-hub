package official.xinhub.assistant.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.delay
import official.xinhub.assistant.R
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.Void
import official.xinhub.assistant.ui.theme.XinDisplay

private val STEPS = listOf(
    "Warming the bench",
    "Loading hero archive",
    "Verifying script catalog",
    "Ready to inject",
)

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var step by remember { mutableIntStateOf(0) }

    val enter by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "enter",
    )
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.86f,
        animationSpec = tween(620, easing = FastOutSlowInEasing),
        label = "scale",
    )
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress",
    )

    val loop = rememberInfiniteTransition(label = "splash")
    val spin by loop.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    val pulse by loop.animateFloat(
        0.35f, 0.9f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val shimmer by loop.animateFloat(
        -1f, 2f,
        infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )

    LaunchedEffect(Unit) {
        started = true
        for (i in STEPS.indices) {
            step = i
            delay(380)
        }
        delay(220)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B1430), Void, Color(0xFF04060C)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(enter)
                .scale(scale)
                .padding(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(132.dp)) {
                    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = Line,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke,
                    )
                    drawArc(
                        color = Cyan.copy(alpha = pulse),
                        startAngle = spin,
                        sweepAngle = 110f,
                        useCenter = false,
                        style = stroke,
                    )
                    drawArc(
                        color = Cyan.copy(alpha = 0.35f),
                        startAngle = spin + 180f,
                        sweepAngle = 60f,
                        useCenter = false,
                        style = stroke,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Panel)
                        .border(1.5.dp, Cyan.copy(alpha = 0.55f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.hub_icon),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(
                "XIN HUB",
                color = TextHi,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                fontFamily = XinDisplay,
                letterSpacing = 3.sp,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "MLBB COMPANION",
                color = Cyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            )

            Spacer(Modifier.height(30.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Panel)
                    .border(0.5.dp, Line, RoundedCornerShape(50)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Cyan.copy(alpha = 0.65f), Cyan),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(5.dp)
                        .graphicsLayer { translationX = shimmer * 220f }
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.22f)),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                STEPS[step],
                color = TextLow,
                fontSize = 10.5.sp,
                letterSpacing = 0.4.sp,
            )
        }
    }
}
