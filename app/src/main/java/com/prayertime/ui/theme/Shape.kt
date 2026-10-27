package com.prayertime.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Konfigurasi bentuk (Shapes) dan radius sudut (Corner Radii) Material 3
 * yang disesuaikan secara konsisten di seluruh aplikasi Jadwal Shalat dan Widget.
 */

// ============================================================================
// Material 3 Shapes & Corner Radii
// ============================================================================

val PrayerShapes = Shapes(
    // ExtraSmall (6dp): Tag mikro seperti lencana "Akurat" dan tag status "AKTIF"
    extraSmall = RoundedCornerShape(6.dp),

    // Small (10dp): Lencana aksi standar dan tag status ringkas
    small = RoundedCornerShape(10.dp),

    // Medium (14dp): Kapsul tanggal Hijriah ("14 Syawal 1445 H") dan sub-kartu Glance Widget
    medium = RoundedCornerShape(14.dp),

    // Large (18dp): Kartu daftar timeline shalat dan kartu ringkasan Kiblat/Hisab
    large = RoundedCornerShape(18.dp),

    // ExtraLarge (26dp): Kartu hero shalat berikutnya dan kontainer terluar Glance Widget
    extraLarge = RoundedCornerShape(26.dp)
)

/**
 * Token bentuk sudut yang dapat digunakan kembali secara konsisten pada komponen antarmuka.
 */
object PrayerCornerTokens {
    /** Radius lengkungan kartu hero shalat (26dp) */
    val HeroCard = RoundedCornerShape(26.dp)

    /** Radius lengkungan kartu baris jadwal shalat (18dp) */
    val TimelineCard = RoundedCornerShape(18.dp)

    /** Radius lengkungan kartu aksi ringkas (18dp) */
    val QuickInfoCard = RoundedCornerShape(18.dp)

    /** Radius lengkungan kontainer luar widget (24dp) */
    val WidgetContainer = RoundedCornerShape(24.dp)

    /** Radius lengkungan kartu kiri hero widget (18dp) */
    val WidgetLeftCard = RoundedCornerShape(18.dp)

    /** Radius lengkungan kartu mini shalat berikutnya pada widget (12dp) */
    val WidgetMiniCard = RoundedCornerShape(12.dp)

    /** Radius bentuk kapsul penuh / pill (100dp) */
    val Pill = RoundedCornerShape(100.dp)

    /** Bentuk indikator bilah vertikal penanda shalat aktif */
    val ActiveIndicatorBar = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 2.dp, bottomEnd = 2.dp)
}
