package com.prayertime.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.ui.theme.PrayerCornerTokens
import com.prayertime.ui.theme.prayerColors

/**
 * Bottom Sheet Tasbih Digital interaktif:
 * - Counter berlingkaran besar dengan animasi tekan membal (*spring bounce effect*).
 * - Efek umpan balik getar haptik ringan ([HapticFeedbackType.LongPress]) setiap kali ditekan.
 * - Pemilih lafadz bacaan dzikir (Subhanallah, Alhamdulillah, Allahu Akbar, dll).
 * - Target default 33x dengan indikator pencapaian visual dan tombol Reset.
 *
 * @param onDismissRequest Callback saat dialog ditutup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihModalSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    val dzikirOptions = listOf(
        "Subhanallah (Maha Suci Allah)",
        "Alhamdulillah (Segala Puji Bagi Allah)",
        "AllahuAkbar (Allah Maha Besar)",
        "Astaghfirullah (Aku Memohon Ampun)",
        "Laailaahaillallah"
    )
    var selectedDzikirIndex by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(0) }
    val targetCount = 33
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "ButtonScale"
    )

    val isTargetReached = count >= targetCount

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header & Reset Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasbih Digital",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )

                // Tombol Reset
                OutlinedButton(
                    onClick = {
                        count = 0
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    shape = PrayerCornerTokens.Pill,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector Dzikir (Scrollable Chips)
            ScrollableTabRow(
                selectedTabIndex = selectedDzikirIndex,
                edgePadding = 0.dp,
                divider = {},
                indicator = {},
                containerColor = Color.Transparent
            ) {
                dzikirOptions.forEachIndexed { index, text ->
                    val isSelected = index == selectedDzikirIndex
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedDzikirIndex = index
                            count = 0
                        },
                        label = { Text(text.split(" ").first(), fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = PrayerCornerTokens.Pill,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = dzikirOptions[selectedDzikirIndex],
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lingkaran Besar Counter Interaktif (Tekan untuk Menghitung)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        if (isTargetReached) MaterialTheme.colorScheme.primary else MaterialTheme.prayerColors.subtlePillBg
                    )
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        count++
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(isPressed) {
                    if (isPressed) {
                        kotlinx.coroutines.delay(100)
                        isPressed = false
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTargetReached) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Target: $targetCount",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = if (isTargetReached) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Indikator Target Tercapai
            AnimatedVisibility(visible = isTargetReached) {
                Row(
                    modifier = Modifier
                        .clip(PrayerCornerTokens.Pill)
                        .background(MaterialTheme.prayerColors.subtlePillBg)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Target 33x Selesai! Alhamdulillah",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sentuh lingkaran besar untuk berdzikir",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}
