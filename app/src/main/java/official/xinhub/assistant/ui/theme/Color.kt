package official.xinhub.assistant.ui.theme

import androidx.compose.ui.graphics.Color

val Void = Color(0xFF080B10)
val Deep = Color(0xFF0C1018)
val Panel = Color(0xFF121722)
val PanelHi = Color(0xFF182030)
val Line = Color(0xFF222B3C)

val Gold = Color(0xFFEAB308)
val GoldDeep = Color(0xFF926E06)
val Violet = Color(0xFFEAB308)
val VioletDeep = Color(0xFF854D0E)
val Cyan = Color(0xFFEAB308)
val CyanDeep = Color(0xFF713F12)
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
