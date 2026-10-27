package com.prayertime.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Konfigurasi tipografi Material 3 yang disesuaikan untuk hierarki tampilan aplikasi Jadwal Shalat.
 * Mengatur bobot, ukuran, spasi baris, dan penjarakan huruf (letter spacing) untuk:
 * - Jam digital besar kartu hero (48sp)
 * - Judul shalat & derajat arah kiblat
 * - Judul baris jadwal, tanggal Hijriah/Masehi, dan label status.
 */
val PrayerTypography = Typography(
    // Display 48sp: Huge digital clock in Hero Prayer Card ("15:24")
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.0).sp
    ),
    // Display 32sp: Prominent prayer title in Hero Card ("ASHAR")
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.5.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp
    ),
    // Headline 22sp: Prominent stats such as Qibla degrees ("295°")
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    // Title 18sp: Section Headers like "Jadwal Shalat Hari Ini", Location Name
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // Title 16sp: Prayer item titles in timeline ("Subuh", "Dzuhur", "Ashar")
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    // Title 13sp: Category labels like "SHALAT BERIKUTNYA", Card headings
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.6.sp
    ),
    // Body 15sp: Standard readable text, time strings ("04:38 WIB")
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    // Body 13sp: Subtitles, prayer description ("Waktu Masuk Ashar Awal • 4 Rakaat")
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // Body 12sp: Subtle footnotes ("Dari posisi Jakarta", "Selesai", "Dalam 2j 28m")
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Label 14sp: Action buttons ("Mulai"), Seminggu selector
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    // Label 12sp: Pill tags ("14 Syawal 1445 H", "Tersisa 01 jam 45 menit")
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Label 10sp: Micro indicator badges ("AKTIF", "Akurat")
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
