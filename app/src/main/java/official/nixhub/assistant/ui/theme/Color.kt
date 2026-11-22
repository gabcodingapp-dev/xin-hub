package official.nixhub.assistant.ui.theme

import androidx.compose.ui.graphics.Color

val Void = Color(0xFF07060F)
val Deep = Color(0xFF0C0A18)
val Panel = Color(0xFF121022)
val PanelHi = Color(0xFF191630)
val Line = Color(0xFF2A2444)

val Violet = Color(0xFF8B5CF6)
val VioletDeep = Color(0xFF5B21B6)
val Cyan = Color(0xFF22D3EE)
val CyanDeep = Color(0xFF0E7490)
val Rose = Color(0xFFF472B6)
val Amber = Color(0xFFFBBF24)
val Lime = Color(0xFFA3E635)

val TextHi = Color(0xFFF3F1FF)
val TextMid = Color(0xFFB9B3D4)
val TextLow = Color(0xFF7C769B)
val Danger = Color(0xFFFF5C7A)
val Good = Color(0xFF34D399)

fun tierColor(tier: String?): Color = when (tier?.lowercase()) {
    "default" -> TextMid
    "common" -> Color(0xFF9CA3AF)
    "exceptional", "elite" -> Color(0xFF60A5FA)
    "deluxe", "special" -> Color(0xFFA78BFA)
    "exquisite", "epic" -> Color(0xFFF472B6)
    "grand", "legend" -> Color(0xFFFBBF24)
    "mythic", "collector" -> Color(0xFFEF4444)
    "fanmade", "custom" -> Cyan
    "painted" -> Lime
    else -> Violet
}
