package official.nixhub.assistant.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import official.nixhub.assistant.R

/** Cinzel carries the medieval slab look; Jakarta keeps small text readable. */
val XinDisplay = FontFamily(
    Font(R.font.cinzel_decorative_bold, FontWeight.Bold),
    Font(R.font.cinzel_decorative_bold, FontWeight.Black),
)

val XinBody = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)
