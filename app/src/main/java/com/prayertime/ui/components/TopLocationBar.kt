package com.prayertime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.ui.theme.PrayerCornerTokens
import com.prayertime.ui.theme.prayerColors

/**
 * Bilah atas (Top Bar) yang menampilkan informasi penanggalan dan status lokasi:
 * - Kapsul tanggal Hijriah & tanggal Masehi lengkap.
 * - Nama kota/kabupaten yang ringkas dengan lencana "Akurat".
 * - Tombol aksi cepat untuk membuka filter metode hisab dan pengaturan.
 *
 * @param hijriDate Teks tanggal Hijriah (misal: "14 Syawal 1445 H").
 * @param gregorianDate Teks tanggal Masehi (misal: "Ahad, 27 Oktober 2026").
 * @param locationName Nama kota/daerah aktif.
 * @param accuracyTag Label status akurasi (misal: "Akurat").
 * @param onLocationClick Callback saat pengguna mengetuk nama lokasi.
 * @param onFilterClick Callback tombol metode hisab.
 * @param onSettingsClick Callback tombol navigasi ke layar pengaturan.
 */
@Composable
fun TopLocationBar(
    hijriDate: String,
    gregorianDate: String,
    locationName: String,
    accuracyTag: String,
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Row 1: Hijri Date Badge & Gregorian Date
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hijri Pill Tag
            Box(
                modifier = Modifier
                    .clip(PrayerCornerTokens.Pill)
                    .background(MaterialTheme.prayerColors.subtlePillBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hijriDate,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.prayerColors.subtlePillText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    )
                )
            }

            // Separator Dot
            Text(
                text = "•",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Gregorian Date
            Text(
                text = gregorianDate,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        // Normalisasi nama daerah agar hemat ruang (ganti "Kabupaten " jadi "Kab. ", buang ", Indonesia")
        val displayLocation = locationName
            .replace("Kabupaten ", "Kab. ")
            .replace("Kota Administrasi ", "Kota ")
            .removeSuffix(", Indonesia")
            .removeSuffix(", ID")
            .trim()

        // Row 2: Location Selector & Action Icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Group (Pin + Teks Lokasi + Chevron + Badge Akurat)
            // Mengambil seluruh sisa ruang layar secara maksimal tanpa terbagi 50% oleh spacer kosong
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onLocationClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location Pin Icon
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Lokasi",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                // Teks Nama Lokasi (Prioritas ruang maksimal)
                Text(
                    text = displayLocation,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(3.dp))

                // Dropdown Chevron
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Pilih Lokasi",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Badge Akurat (Terkunci horizontal, berdampingan dengan chevron)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = accuracyTag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons: Filter (Metode Hisab) & Settings (Fixed di sisi kanan layar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Filter Metode Hisab",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Pengaturan",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
