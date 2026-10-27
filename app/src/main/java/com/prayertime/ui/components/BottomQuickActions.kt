package com.prayertime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.ui.model.NavigationItem

/**
 * Bottom Navigation Bar utama aplikasi Jadwal Shalat dengan 4 tab:
 * 1. Shalat (Dashboard utama & linimasa)
 * 2. Kiblat (Kompas arah Ka'bah interaktif)
 * 3. Doa (Kumpulan doa harian & dzikir)
 * 4. Pengaturan (Konfigurasi kota & metode hisab)
 *
 * @param selectedItem Item tab yang sedang aktif saat ini ([NavigationItem]).
 * @param onItemSelected Callback saat salah satu item tab ditekan.
 */
@Composable
fun BottomQuickActions(
    selectedItem: NavigationItem,
    modifier: Modifier = Modifier,
    onItemSelected: (NavigationItem) -> Unit = {}
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationItem.values().forEach { item ->
            val isSelected = item == selectedItem

            val icon = when (item) {
                NavigationItem.SHALAT -> Icons.Outlined.Schedule
                NavigationItem.KIBLAT -> Icons.Outlined.Explore
                NavigationItem.DOA -> Icons.Outlined.MenuBook
                NavigationItem.PENGATURAN -> Icons.Outlined.Settings
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
