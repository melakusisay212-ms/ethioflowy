package et.ethioflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Colors matching the provided UI screenshots
val NavyDark = Color(0xFF0F1428)
val NavyCard = Color(0xFF1B1F3B)
val NavySoft = Color(0xFF252A45)
val AccentGreen = Color(0xFF4CAF50)
val AccentBlue = Color(0xFF5B8DEF)
val AccentRed = Color(0xFFE57373)
val AccentOrange = Color(0xFFFFB74D)
val SurfaceLight = Color(0xFFF7F8FC)
val CardWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1A1D2E)
val TextSecondary = Color(0xFF6B7280)
val TextOnDark = Color(0xFFFFFFFF)
val TextOnDarkSecondary = Color(0xFFB0B5C8)

private val LightColorScheme = lightColorScheme(
    primary = NavyCard,
    onPrimary = TextOnDark,
    secondary = AccentBlue,
    background = SurfaceLight,
    surface = CardWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFEEF0F6),
    outline = Color(0xFFE2E5EF)
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextOnDark,
    secondary = AccentGreen,
    background = NavyDark,
    surface = NavyCard,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    surfaceVariant = NavySoft,
    outline = Color(0xFF3A3F5C)
)

@Composable
fun EthioFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
            headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
            titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
            bodyLarge = TextStyle(fontSize = 16.sp),
            bodyMedium = TextStyle(fontSize = 14.sp),
            labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
        ),
        content = content
    )
}
