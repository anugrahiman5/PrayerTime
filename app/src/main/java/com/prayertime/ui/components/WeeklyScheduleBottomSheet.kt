package com.prayertime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.data.repository.DayPrayerSchedule
import com.prayertime.ui.theme.PrayerCornerTokens
import com.prayertime.ui.theme.prayerColors

/**
 * Bottom Sheet jadwal shalat 7 hari (Pekanan):
 * Menampilkan daftar vertikal jadwal shalat untuk 7 hari ke depan (Subuh, Terbit, Dzuhur, Ashar, Maghrib, Isya),
 * lengkap dengan penanggalan Hijriah dan nama hari Islam (Ahad - Sabtu),
 * serta menyorot hari ini dengan bingkai khusus.
 *
 * @param weeklyList Daftar data jadwal shalat per hari ([DayPrayerSchedule]).
 * @param cityName Nama kota/daerah aktif.
 * @param onDismissRequest Callback saat dialog ditutup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScheduleBottomSheet(
    weeklyList: List<DayPrayerSchedule>,
    cityName: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Jadwal Shalat 7 Hari",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.5.sp
                        )
                    )
                    Text(
                        text = "Wilayah $cityName",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vertikal List Jadwal 7 Hari
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(weeklyList) { daySchedule ->
                    val isToday = daySchedule.isToday

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isToday) 1.5.dp else 1.dp,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = PrayerCornerTokens.TimelineCard
                            ),
                        shape = PrayerCornerTokens.TimelineCard,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isToday) MaterialTheme.prayerColors.subtlePillBg else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            // Baris Tanggal
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = daySchedule.gregorianFormatted,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (isToday) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(PrayerCornerTokens.Pill)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "HARI INI",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = daySchedule.hijriFormatted,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid / Row Waktu 5 Shalat Fardhu + Terbit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PrayerTimeSlot("Subuh", daySchedule.subuh)
                                PrayerTimeSlot("Terbit", daySchedule.terbit)
                                PrayerTimeSlot("Dzuhur", daySchedule.dzuhur)
                                PrayerTimeSlot("Ashar", daySchedule.ashar)
                                PrayerTimeSlot("Maghrib", daySchedule.maghrib)
                                PrayerTimeSlot("Isya", daySchedule.isya)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeSlot(name: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
