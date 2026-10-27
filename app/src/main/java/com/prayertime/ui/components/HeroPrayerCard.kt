package com.prayertime.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.ui.model.HeroPrayerUiModel
import com.prayertime.ui.theme.DotActiveGreen
import com.prayertime.ui.theme.PrayerCornerTokens
import com.prayertime.ui.theme.prayerColors

/**
 * Kartu visual utama (Hero Card) di bagian atas layar dashboard.
 * Menampilkan:
 * - Kategori dan lencana hitung mundur dengan animasi berkedip lembut (*pulsing live dot*).
 * - Judul shalat berikutnya ("ASHAR"), jam digital besar ("15:24 WIB"), dan deskripsi rakaat.
 * - Bilah progres linimasa gradien yang menunjukkan proporsi waktu yang telah terlewati.
 * - Informasi stasiun shalat sebelumnya, stasiun saat ini, dan stasiun mendatang.
 *
 * @param model Model data [HeroPrayerUiModel].
 * @param modifier Modifier Jetpack Compose.
 */
@Composable
fun HeroPrayerCard(
    model: HeroPrayerUiModel,
    modifier: Modifier = Modifier
) {
    val extendedColors = MaterialTheme.prayerColors

    // Subtle pulsing animation for the live status dot
    val infiniteTransition = rememberInfiniteTransition(label = "DotPulseTransition")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = PrayerCornerTokens.HeroCard,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            extendedColors.heroGradientStart,
                            extendedColors.heroGradientEnd
                        )
                    )
                )
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Header Row: "SHALAT BERIKUTNYA" + Countdown Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = model.categoryLabel.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = extendedColors.heroSubtext,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    // Pill Badge: Live Dot + Countdown Text
                    Row(
                        modifier = Modifier
                            .clip(PrayerCornerTokens.Pill)
                            .background(extendedColors.heroBadgePillBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(DotActiveGreen.copy(alpha = dotAlpha))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = model.countdownFormatted,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Middle Row: Prayer Name & Description | Big Digital Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Name & Description
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = model.prayerName,
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = model.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = extendedColors.heroSubtext,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    // Right Column: Big Time & WIB
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = model.timeString,
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 46.sp,
                                letterSpacing = (-1).sp
                            )
                        )
                        Text(
                            text = model.timeZone,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = extendedColors.heroSubtext,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Bottom Section: Progress Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.prevPrayerInfo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = extendedColors.heroSubtext.copy(alpha = 0.85f),
                            fontSize = 11.5.sp
                        )
                    )
                    Text(
                        text = model.currentStationInfo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                    Text(
                        text = model.nextPrayerInfo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = extendedColors.heroSubtext.copy(alpha = 0.85f),
                            fontSize = 11.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Gradient Progress Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(extendedColors.heroProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(model.progressFraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        extendedColors.heroProgressStart,
                                        extendedColors.heroProgressEnd
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
