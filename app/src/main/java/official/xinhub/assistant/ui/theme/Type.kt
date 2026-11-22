package official.xinhub.assistant.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import official.xinhub.assistant.R

/** Jakarta carries display weight; Inter keeps dense catalog text readable. */
val XinDisplay = FontFamily(
    Font(R.font.plus_jakarta_variable, FontWeight.Medium),
    Font(R.font.plus_jakarta_variable, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_variable, FontWeight.Bold),
    Font(R.font.plus_jakarta_variable, FontWeight.Black),
)

val XinBody = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.Medium),
    Font(R.font.inter_variable, FontWeight.SemiBold),
    Font(R.font.inter_variable, FontWeight.Bold),
)
