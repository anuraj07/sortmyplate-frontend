package io.sortmyplate.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF0A0F1D)
val CardDark = Color(0xFF131B2E)
val CardBorder = Color(0xFF1E293B)
val CyanAccent = Color(0xFF38BDF8)
val EmeraldGreen = Color(0xFF10B981)
val RoseRed = Color(0xFFF43F5E)
val AmberYellow = Color(0xFFF59E0B)
val PurpleAccent = Color(0xFF8B5CF6)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF0F172A),
    secondary = PurpleAccent,
    background = BgDark,
    surface = CardDark,
    onSurface = TextPrimary,
    onBackground = TextPrimary
)
