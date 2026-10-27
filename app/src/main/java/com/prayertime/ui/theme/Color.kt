package com.prayertime.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Definisi palet warna dan token desain untuk aplikasi Jadwal Shalat.
 * Mencakup palet utama bertema hijau Islami, warna aksen, warna netral (Light & Dark mode),
 * serta semantic color extension khusus untuk komponen kartu jadwal dan widget.
 */

// ============================================================================
// Core Palette Tokens
// ============================================================================

// Primary Islamic Green Shades
val IslamicGreenPrimary = Color(0xFF1E4636)       // Warna utama brand hijau zamrud / pinus
val IslamicGreenDark = Color(0xFF143427)          // Gradien gelap kartu hero shalat
val IslamicGreenLight = Color(0xFF2E6550)         // Aksen hijau terang
val IslamicGreenContainer = Color(0xFFEAF5EF)    // Latar belakang kartu shalat yang sedang aktif
val IslamicGreenOnContainer = Color(0xFF102D21)

// Accent & Supporting Colors
val DirectionAccentRed = Color(0xFFBA3C2A)        // Penanda arah mata angin kiblat (Barat Laut)
val DotActiveGreen = Color(0xFF44CF7D)            // Indikator animasi titik hitung mundur
val ProgressFillStart = Color(0xFFA2DFC0)         // Titik awal gradien bar kemajuan shalat
val ProgressFillEnd = Color(0xFFEE9B85)           // Titik akhir gradien bar kemajuan shalat

// Neutral & Surface Shades (Light Mode)
val SurfaceLight = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF5F8F5)
val SurfaceVariantLight = Color(0xFFEFF4F0)
val OnSurfaceLight = Color(0xFF1A231E)
val OnSurfaceVariantLight = Color(0xFF6B7D72)
val OutlineLight = Color(0xFFE2EBE4)
val OutlineSubtleLight = Color(0xFFEEF3EF)

// Neutral & Surface Shades (Dark Mode)
val PrimaryDark = Color(0xFF63DB9E)
val OnPrimaryDark = Color(0xFF003820)
val PrimaryContainerDark = Color(0xFF163C2D)
val OnPrimaryContainerDark = Color(0xFFA2F5C8)
val BackgroundDark = Color(0xFF0F1512)
val SurfaceDark = Color(0xFF17201B)
val SurfaceVariantDark = Color(0xFF202C25)
val OnSurfaceDark = Color(0xFFE1E8E3)
val OnSurfaceVariantDark = Color(0xFFA1B3A8)
val OutlineDark = Color(0xFF2E3E34)
val OutlineSubtleDark = Color(0xFF25332A)

// Glance Widget Specific Palette Tokens
val WidgetContainerExpandedBg = Color(0xFFDCE7DF)
val WidgetCardBg = Color(0xFFFFFFFF)
val WidgetPillBg = Color(0xFFDEEAE1)
val WidgetTextPrimary = Color(0xFF1E4636)
val WidgetTextSecondary = Color(0xFF6B7D72)

// ============================================================================
// Extended Theme Colors for Prayer App Semantics
// ============================================================================

/**
 * Kumpulan warna khusus (custom semantic tokens) di luar Material 3 standar,
 * digunakan untuk elemen spesifik seperti kartu hero waktu shalat, status waktu yang telah lewat,
 * dan aksen arah kompas kiblat.
 */
@Immutable
data class PrayerExtendedColors(
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val heroSubtext: Color,
    val heroBadgePillBg: Color,
    val heroProgressTrack: Color,
    val heroProgressStart: Color,
    val heroProgressEnd: Color,
    val activePrayerCardBg: Color,
    val activePrayerCardBorder: Color,
    val activeIndicatorBar: Color,
    val passedPrayerText: Color,
    val passedPrayerIcon: Color,
    val qiblaAccent: Color,
    val subtlePillBg: Color,
    val subtlePillText: Color
)

val LocalPrayerExtendedColors = staticCompositionLocalOf {
    PrayerExtendedColors(
        heroGradientStart = IslamicGreenPrimary,
        heroGradientEnd = IslamicGreenDark,
        heroSubtext = Color(0xFF93BCA9),
        heroBadgePillBg = Color(0xFF123325),
        heroProgressTrack = Color(0xFF133023),
        heroProgressStart = ProgressFillStart,
        heroProgressEnd = ProgressFillEnd,
        activePrayerCardBg = IslamicGreenContainer,
        activePrayerCardBorder = IslamicGreenPrimary,
        activeIndicatorBar = IslamicGreenPrimary,
        passedPrayerText = Color(0xFF708076),
        passedPrayerIcon = Color(0xFFA1AFA7),
        qiblaAccent = DirectionAccentRed,
        subtlePillBg = Color(0xFFEBF2ED),
        subtlePillText = IslamicGreenPrimary
    )
}

val DarkPrayerExtendedColors = PrayerExtendedColors(
    heroGradientStart = Color(0xFF17382B),
    heroGradientEnd = Color(0xFF0F261D),
    heroSubtext = Color(0xFF86AC99),
    heroBadgePillBg = Color(0xFF0D2118),
    heroProgressTrack = Color(0xFF132D21),
    heroProgressStart = Color(0xFF72CAA0),
    heroProgressEnd = Color(0xFFE28B75),
    activePrayerCardBg = Color(0xFF1B382B),
    activePrayerCardBorder = Color(0xFF63DB9E),
    activeIndicatorBar = Color(0xFF63DB9E),
    passedPrayerText = Color(0xFF7B8D83),
    passedPrayerIcon = Color(0xFF5B6C63),
    qiblaAccent = Color(0xFFE56A55),
    subtlePillBg = Color(0xFF1B2B22),
    subtlePillText = Color(0xFF94E6B9)
)
