package com.prayertime.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Skema warna Light Mode yang dioptimalkan untuk nuansa hijau Islami yang bersih dan kontras.
 */
private val LightColorScheme = lightColorScheme(
    primary = IslamicGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = IslamicGreenContainer,
    onPrimaryContainer = IslamicGreenOnContainer,
    secondary = DirectionAccentRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDE8E5),
    onSecondaryContainer = Color(0xFF5D1208),
    tertiary = DotActiveGreen,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineSubtleLight
)

/**
 * Skema warna Dark Mode yang nyaman di mata untuk kondisi minim cahaya dan malam hari.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = Color(0xFFE56A55),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4C1D17),
    onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = DotActiveGreen,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineSubtleDark
)

/**
 * Properti ekstensi untuk mengakses token warna khusus aplikasi shalat ([PrayerExtendedColors])
 * secara langsung melalui [MaterialTheme.prayerColors].
 */
val MaterialTheme.prayerColors: PrayerExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPrayerExtendedColors.current

/**
 * Tema utama aplikasi Jadwal Shalat (Material Design 3).
 * Mengatur palet warna, tipografi, bentuk, kontrol status bar sistem,
 * serta menyuplai token warna tambahan melalui [CompositionLocalProvider].
 *
 * @param darkTheme Menentukan apakah tema gelap aktif (default mendeteksi pengaturan sistem).
 * @param content Konten composable yang dinaungi tema.
 */
@Composable
fun PrayerTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkPrayerExtendedColors else LocalPrayerExtendedColors.current

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalPrayerExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PrayerTypography,
            shapes = PrayerShapes,
            content = content
        )
    }
}
