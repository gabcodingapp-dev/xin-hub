package official.nixhub.assistant.ui.theme

import androidx.compose.ui.graphics.Color

val Void = Color(0xFF090A0E)
val Deep = Color(0xFF0E1015)
val Panel = Color(0xFF14161C)
val PanelHi = Color(0xFF1B1E26)
val Line = Color(0xFF262A33)

val Violet = Color(0xFF8B5CF6)
val VioletDeep = Color(0xFF5B21B6)
val Cyan = Color(0xFF3DDC97)
val CyanDeep = Color(0xFF187A56)
val Rose = Color(0xFFF472B6)
val Amber = Color(0xFFFBBF24)
val Lime = Color(0xFFB6F36A)

val TextHi = Color(0xFFF5F6F8)
val TextMid = Color(0xFFA9AEBB)
val TextLow = Color(0xFF6F7582)
val Danger = Color(0xFFFF6B7A)
val Good = Color(0xFF3DDC97)

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
    else -> TextLow
}
