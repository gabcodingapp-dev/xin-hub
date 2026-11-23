package official.xinhub.assistant.ui.theme

import androidx.compose.ui.graphics.Color

/** Deep navy arena with MLBB gold — matches the reference companion's visual language. */
val Void = Color(0xFF060A14)
val Deep = Color(0xFF0A1020)
val Panel = Color(0xFF0E1628)
val PanelHi = Color(0xFF15203A)
val Line = Color(0xFF243356)

val Gold = Color(0xFFFFD60A)
val GoldDeep = Color(0xFF9A7B00)
val Violet = Color(0xFFFFD60A)
val VioletDeep = Color(0xFF7A5C00)
val Cyan = Color(0xFFFFD60A)
val CyanDeep = Color(0xFF6B5200)
val Rose = Color(0xFFFF5C8A)
val Amber = Color(0xFFFFC53D)
val Lime = Color(0xFF7CFC9A)
val Sky = Color(0xFF4DA3FF)

val TextHi = Color(0xFFF7F9FC)
val TextMid = Color(0xFFAEB8CC)
val TextLow = Color(0xFF6E7A94)
val Danger = Color(0xFFFF5C6C)
val Good = Color(0xFF3DDC97)

fun tierColor(tier: String?): Color = when (tier?.lowercase()) {
    "default" -> TextMid
    "common" -> Color(0xFF9CA3AF)
    "exceptional", "elite" -> Sky
    "deluxe", "special" -> Color(0xFFA78BFA)
    "exquisite", "epic" -> Rose
    "grand", "legend" -> Amber
    "mythic", "collector" -> Color(0xFFFF6B4A)
    "fanmade", "custom" -> Gold
    "painted" -> Lime
    else -> TextLow
}
