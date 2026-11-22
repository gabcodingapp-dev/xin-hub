package official.xinhub.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import official.xinhub.assistant.ui.theme.Cyan
import official.xinhub.assistant.ui.theme.Line
import official.xinhub.assistant.ui.theme.Panel
import official.xinhub.assistant.ui.theme.PanelHi
import official.xinhub.assistant.ui.theme.TextHi
import official.xinhub.assistant.ui.theme.TextLow
import official.xinhub.assistant.ui.theme.TextMid
import official.xinhub.assistant.ui.theme.XinDisplay

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Panel)
                .border(1.dp, Line, RoundedCornerShape(9.dp))
                .pressable(onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TextMid,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = TextHi,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = XinDisplay,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = Cyan,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun SegmentedTabs(
    items: List<Triple<String, String, Int>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for ((id, label, count) in items) {
            val active = id == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Cyan else Panel)
                    .border(1.dp, if (active) Cyan else Line, RoundedCornerShape(10.dp))
                    .pressable { onSelect(id) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$label · $count",
                    color = if (active) Color(0xFF090B10) else TextMid,
                    fontSize = 9.5.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun PrimaryAction(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val fill = if (enabled) {
        Brush.horizontalGradient(listOf(Cyan, Color(0xFFFDE047)))
    } else {
        Brush.horizontalGradient(listOf(Line, Line))
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(fill)
            .pressable(onClick, enabled),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) Color(0xFF090B10) else TextLow,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                color = if (enabled) Color(0xFF090B10) else TextLow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun GhostAction(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Cyan,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.38f), RoundedCornerShape(50))
            .pressable(onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun MenuThumb(
    url: String?,
    modifier: Modifier = Modifier,
    scale: ContentScale = ContentScale.Crop,
    width: Int = 240,
    height: Int = 300,
    radius: Dp = 10.dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(PanelHi),
    ) {
        NetImage(url, Modifier.fillMaxSize(), scale, width, height)
    }
}

@Composable
fun SectionTitle(
    title: String,
    count: Int? = null,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = TextHi,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = XinDisplay,
            )
            if (count != null) {
                Text("$count entries", color = TextLow, fontSize = 9.sp)
            }
        }
        if (action != null) {
            Row(
                modifier = Modifier.pressable(onAction),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(action, color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

@Composable
fun ColumnScope.SheetHandle() {
    Box(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(width = 34.dp, height = 3.dp)
            .clip(RoundedCornerShape(50))
            .background(Line),
    )
    Spacer(Modifier.height(10.dp))
}
