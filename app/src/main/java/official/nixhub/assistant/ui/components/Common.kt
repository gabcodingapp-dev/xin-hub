package official.nixhub.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import official.nixhub.assistant.Xin
import official.nixhub.assistant.ui.theme.Line
import official.nixhub.assistant.ui.theme.Panel
import official.nixhub.assistant.ui.theme.PanelHi
import official.nixhub.assistant.ui.theme.TextHi
import official.nixhub.assistant.ui.theme.TextLow
import official.nixhub.assistant.ui.theme.TextMid
import official.nixhub.assistant.ui.theme.Void
import official.nixhub.assistant.ui.theme.XinBody

@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.965f else 1f, tween(90))
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

@Composable
fun XinBackdrop(content: @Composable () -> Unit) {
    Box(modifier = Modifier.background(Void)) { content() }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var card = modifier
        .clip(RoundedCornerShape(14.dp))
        .background(Panel)
        .border(1.dp, Line, RoundedCornerShape(14.dp))
    if (onClick != null) card = card.pressable(onClick)
    Box(card) { content() }
}

@Composable
fun Pill(text: String, color: Color, filled: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color else color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text,
            color = if (filled) Void else color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextHi, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = TextLow, fontSize = 10.5.sp)
        }
        if (action != null) {
            Text(
                action,
                color = Color(0xFF3DDC97),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onAction() },
            )
        }
    }
}

@Composable
fun NetImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    targetWidth: Int = 320,
    targetHeight: Int = 320,
) {
    if (url.isNullOrBlank()) {
        Box(modifier.background(PanelHi))
        return
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val request = androidx.compose.runtime.remember(url, targetWidth, targetHeight) {
        ImageRequest.Builder(context)
            .data(url)
            .size(targetWidth, targetHeight)
            .precision(coil.size.Precision.EXACT)
            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
            .build()
    }
    AsyncImage(
        model = request,
        imageLoader = Xin.images,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier,
    )
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Text(value, color = TextHi, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextLow, fontSize = 9.5.sp)
    }
}

@Composable
fun EmptyState(title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = TextHi, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(5.dp))
        Text(body, color = TextLow, fontSize = 11.sp)
    }
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Line))
}

@Composable
fun RowSpacer(width: Int) = Spacer(Modifier.width(width.dp))

@Composable
fun SearchField(value: String, hint: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = TextLow, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, color = TextLow, fontSize = 12.sp)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = TextHi, fontSize = 12.sp, fontFamily = XinBody),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                tint = TextLow,
                modifier = Modifier.size(15.dp).clickable { onChange("") },
            )
        }
    }
}

@Composable
fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) Color(0xFF3DDC97) else Panel)
            .border(1.dp, if (active) Color(0xFF3DDC97) else Line, RoundedCornerShape(50))
            .pressable(onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            color = if (active) Void else TextMid,
            fontSize = 10.5.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
