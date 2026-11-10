package io.sortmyplate.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Airy Botanical Daylight Canvas
val MistBackground = Color(0xFFF3F7F2)
val MistTint = Color(0xFFE6EFE4)
val CreamSurface = Color(0xFFFAFBF7)
val CardWhite = Color(0xFFFFFFFF)
val SageSurface = Color(0xFFEDF4EC)
val CardBorder = Color(0xFFD8E5D6)

// Nixtio Botanical Forest & Emerald Aura
val ForestHeroDeep = Color(0xFF13251B)
val ForestHeroMid = Color(0xFF1D3C2B)
val EmeraldPrimary = Color(0xFF2B9A5B)
val EmeraldGlow = Color(0xFF49C983)

// Bento Tile Gradients
val CrimsonTop = Color(0xFFC63632)
val CrimsonBottom = Color(0xFF7F1417)
val AmberBentoTop = Color(0xFFF09E27)
val AmberBentoBottom = Color(0xFFBC6106)
val SageBentoTop = Color(0xFF86B88B)
val SageBentoBottom = Color(0xFF3D714A)
val SkyBentoTop = Color(0xFF74B7EA)
val SkyBentoBottom = Color(0xFF3078B7)
val OliveBentoTop = Color(0xFF648F60)
val OliveBentoBottom = Color(0xFF315335)

// Text Hierarchy
val TextPrimary = Color(0xFF141D18)
val TextSecondary = Color(0xFF4C5D54)
val TextMuted = Color(0xFF708077)

// Semantic Status & Spectrum Colors
val EmeraldGreen = EmeraldPrimary
val CyanAccent = EmeraldPrimary
val RoseRed = Color(0xFFE14E3A)
val CoralRed = RoseRed
val AmberYellow = Color(0xFFE2911D)
val AmberGold = AmberYellow
val LimeHealth = Color(0xFF94BD42)
val VioletRange = Color(0xFF6F42C1)
val PurpleAccent = VioletRange

// Dark surface tokens preserved for Insights Bento Canvas & Network Inspector
val BgDark = Color(0xFF13251B)
val CardDark = Color(0xFF1D3125)
val InspectorTextPrimary = Color(0xFFF3F7F2)
val InspectorTextSecondary = Color(0xFFC2D4C6)
val InspectorTextMuted = Color(0xFF8AA191)
val InspectorBorder = Color(0xFF2D4437)
val InspectorAccent = Color(0xFF49C983)

val LightColorScheme = lightColorScheme(
    primary = ForestHeroDeep,
    onPrimary = Color.White,
    secondary = EmeraldPrimary,
    onSecondary = Color.White,
    background = MistBackground,
    surface = CardWhite,
    surfaceVariant = CreamSurface,
    onSurface = TextPrimary,
    onBackground = TextPrimary,
    outline = CardBorder
)

val DarkColorScheme = darkColorScheme(
    primary = EmeraldGlow,
    onPrimary = ForestHeroDeep,
    secondary = SageBentoTop,
    background = BgDark,
    surface = CardDark,
    onSurface = Color.White,
    onBackground = Color.White
)
