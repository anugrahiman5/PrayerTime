package com.prayertime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.ui.model.PrayerItemUiModel
import com.prayertime.ui.model.PrayerStatus
import com.prayertime.ui.model.PrayerType
import com.prayertime.ui.model.TasbihQuickUiModel
import com.prayertime.ui.theme.PrayerCornerTokens
import com.prayertime.ui.theme.prayerColors

/**
 * Komponen daftar vertikal linimasa jadwal shalat hari ini (Subuh, Syuruq, Dzuhur, Ashar, Maghrib, Isya).
 * Mendukung:
 * - Tombol aksi kalender "7 Hari" untuk melihat jadwal pekanan.
 * - Penandaan visual status shalat (Aktif / Selesai / Mendatang).
 * - Tombol lonceng interaktif untuk mengaktifkan/menonaktifkan pengingat alarm shalat seketika.
 *
 * @param prayers Daftar item shalat ([PrayerItemUiModel]).
 * @param onSemingguClick Callback pembuka jadwal 7 hari.
 * @param onPrayerClick Callback saat baris shalat ditekan.
 * @param onNotificationToggle Callback saat ikon lonceng pengingat ditekan.
 */
@Composable
fun PrayerTimelineList(
    prayers: List<PrayerItemUiModel>,
    modifier: Modifier = Modifier,
    onSemingguClick: () -> Unit = {},
    onPrayerClick: (PrayerItemUiModel) -> Unit = {},
    onNotificationToggle: (PrayerItemUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jadwal Shalat Hari Ini",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            // Weekly Calendar Action Button ("7 Hari")
            Row(
                modifier = Modifier
                    .clip(PrayerCornerTokens.Pill)
                    .clickable { onSemingguClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Jadwal 7 Hari",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "7 Hari",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Prayer Cards Stack
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            prayers.forEach { prayer ->
                PrayerTimelineItemCard(
                    prayer = prayer,
                    onClick = { onPrayerClick(prayer) },
                    onNotificationToggle = { onNotificationToggle(prayer) }
                )
            }
        }
    }
}

@Composable
fun PrayerTimelineItemCard(
    prayer: PrayerItemUiModel,
    onClick: () -> Unit,
    onNotificationToggle: () -> Unit
) {
    val extendedColors = MaterialTheme.prayerColors
    val isActive = prayer.status == PrayerStatus.ACTIVE
    val isPassed = prayer.status == PrayerStatus.PASSED

    val cardBorder = if (isActive) {
        extendedColors.activePrayerCardBorder
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val cardBackground = if (isActive) {
        extendedColors.activePrayerCardBg
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = cardBorder,
                shape = PrayerCornerTokens.TimelineCard
            ),
        shape = PrayerCornerTokens.TimelineCard,
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Pill Indicator for Active Prayer
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(36.dp)
                        .align(Alignment.CenterStart)
                        .clip(PrayerCornerTokens.ActiveIndicatorBar)
                        .background(extendedColors.activeIndicatorBar)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Prayer Status Icon
                PrayerLeadingIcon(prayer = prayer)

                Spacer(modifier = Modifier.width(14.dp))

                // Prayer Name & Subtitle Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = prayer.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 15.5.sp,
                                color = if (isPassed) {
                                    extendedColors.passedPrayerText
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        )

                        // "AKTIF" pill badge
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(PrayerCornerTokens.Pill)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AKTIF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = prayer.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = if (isPassed) {
                                extendedColors.passedPrayerText.copy(alpha = 0.8f)
                            } else if (isActive) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }

                // Time String & Zone
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = prayer.timeString,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = if (isActive) 19.sp else 16.sp,
                            color = if (isPassed) {
                                extendedColors.passedPrayerText
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    )

                    Text(
                        text = prayer.timeZone,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isPassed) {
                                extendedColors.passedPrayerText.copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Trailing Notification / Action Icon
                PrayerTrailingAction(
                    prayer = prayer,
                    onToggle = onNotificationToggle
                )
            }
        }
    }
}

@Composable
private fun PrayerLeadingIcon(prayer: PrayerItemUiModel) {
    val extendedColors = MaterialTheme.prayerColors

    when (prayer.status) {
        PrayerStatus.PASSED -> {
            if (prayer.type == PrayerType.TERBIT) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "Terbit",
                        tint = extendedColors.passedPrayerIcon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selesai",
                        tint = extendedColors.passedPrayerIcon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        PrayerStatus.ACTIVE -> {
            // Dark green filled circle with white clock icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "Waktu Shalat Aktif",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        PrayerStatus.UPCOMING -> {
            val iconVector = when (prayer.type) {
                PrayerType.MAGHRIB -> Icons.Outlined.WbTwilight
                PrayerType.ISYA -> Icons.Outlined.Bedtime
                else -> Icons.Outlined.Schedule
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.prayerColors.subtlePillBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = prayer.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PrayerTrailingAction(
    prayer: PrayerItemUiModel,
    onToggle: () -> Unit
) {
    if (prayer.type == PrayerType.TERBIT) {
        // Subtle small dot for Syuruq (bukan shalat wajib dengan kumandang adzan)
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.prayerColors.passedPrayerIcon.copy(alpha = 0.5f))
        )
        return
    }

    val isEnabled = prayer.isNotificationEnabled

    // Interactive Bell Icon untuk 5 waktu shalat fardhu (Subuh, Dzuhur, Ashar, Maghrib, Isya)
    // Berfungsi responsif di seluruh state (aktif, akan datang, maupun sudah lewat)
    IconButton(
        onClick = onToggle,
        modifier = Modifier.size(38.dp)
    ) {
        if (isEnabled) {
            // Aktif: Lingkaran hijau lembut dengan ikon lonceng hijau emerald aktif
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.prayerColors.subtlePillBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Pengingat Aktif",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
            }
        } else {
            // Nonaktif / Hening: Lonceng coret dengan warna pudar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = "Pengingat Nonaktif",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

/**
 * Kartu akses cepat tasbih digital di bagian bawah daftar linimasa jadwal shalat.
 *
 * @param tasbihInfo Model data ringkasan tasbih [TasbihQuickUiModel].
 * @param onStartClick Callback saat tombol "Mulai" ditekan untuk membuka bottom sheet counter tasbih.
 */
@Composable
fun TasbihQuickCard(
    tasbihInfo: TasbihQuickUiModel,
    modifier: Modifier = Modifier,
    onStartClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = PrayerCornerTokens.TimelineCard
            ),
        shape = PrayerCornerTokens.TimelineCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Concentric Circle Icon (Tasbih bead / target)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.prayerColors.subtlePillBg),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Target Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tasbihInfo.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tasbihInfo.targetDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // "Mulai" Button (Pill shape, dark green)
            Button(
                onClick = onStartClick,
                shape = PrayerCornerTokens.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = tasbihInfo.buttonLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
