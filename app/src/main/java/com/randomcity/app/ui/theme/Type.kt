package com.randomcity.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.randomcity.app.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val spaceGrotesk = GoogleFont("Space Grotesk")

/** Space Grotesk(downloadable font,离线自动回退默认字体)。 */
val SpaceGroteskFamily = FontFamily(
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Bold)
)

private val defaultTypography = Typography()

/** 展示/标题/标签系列使用 Space Grotesk,正文保持系统字体(中文混排自然)。 */
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    ),
    displayMedium = defaultTypography.displayMedium.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = defaultTypography.displaySmall.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineLarge = defaultTypography.headlineLarge.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium
    ),
    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium
    )
)

/** 城市名展示样式:Space Grotesk Bold 40sp。 */
val CityDisplayStyle: TextStyle
    get() = AppTypography.displayLarge.copy(fontSize = 40.sp)
