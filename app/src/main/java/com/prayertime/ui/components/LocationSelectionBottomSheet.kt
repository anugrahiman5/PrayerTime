package com.prayertime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayertime.data.location.CityPreset
import com.prayertime.ui.theme.PrayerCornerTokens
import com.prayertime.ui.theme.prayerColors
import kotlinx.coroutines.delay

/**
 * Bottom Sheet pemilihan lokasi dan pencarian daerah di Indonesia:
 * - Tombol deteksi otomatis koordinat GPS dengan animasi progres.
 * - Kolom pencarian teks dinamis dengan debounce (500ms) untuk menghemat sumber daya.
 * - Pencarian instan luring pada dataset lokal 514 Kabupaten/Kota dan fallback Geocoder online.
 *
 * @param currentCityName Nama kota/daerah yang saat ini aktif.
 * @param isLoadingGps Status pemuatan koordinat via GPS.
 * @param isSearching Status proses pencarian Geocoder.
 * @param citiesList Daftar kota hasil filter ([CityPreset]).
 * @param onDismissRequest Callback saat dialog ditutup.
 * @param onUseGpsClick Callback aksi klik tombol GPS.
 * @param onSearchQueryChanged Callback saat teks pencarian berubah (dengan debounce).
 * @param onCitySelected Callback saat salah satu kota dipilih dari daftar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionBottomSheet(
    currentCityName: String,
    isLoadingGps: Boolean,
    isSearching: Boolean,
    citiesList: List<CityPreset>,
    onDismissRequest: () -> Unit,
    onUseGpsClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCitySelected: (CityPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    // Debounce 500ms pada pencarian input user
    LaunchedEffect(searchQuery) {
        delay(500L)
        onSearchQueryChanged(searchQuery)
    }

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
            Text(
                text = "Pilih Lokasi & Kota",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tombol Deteksi GPS Otomatis
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PrayerCornerTokens.TimelineCard)
                    .clickable(enabled = !isLoadingGps) { onUseGpsClick() },
                color = MaterialTheme.prayerColors.subtlePillBg,
                shape = PrayerCornerTokens.TimelineCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Deteksi GPS",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gunakan Lokasi Saat Ini (GPS)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isLoadingGps) "Sedang mendeteksi koordinat..." else "Deteksi otomatis akurat dari GPS perangkat",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Pencarian Dinamis Geocoder dengan Debounce
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari 514 kota/kabupaten di Indonesia...", fontSize = 13.5.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Hapus",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (searchQuery.isEmpty()) "Daftar 514 Kabupaten & Kota (${citiesList.size})" else "Hasil Pencarian (${citiesList.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Daftar Kota Hasil Pencarian atau Fallback
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(citiesList, key = { it.id }) { city ->
                    val isSelected = currentCityName.contains(city.name, ignoreCase = true)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCitySelected(city) }
                            .background(if (isSelected) MaterialTheme.prayerColors.subtlePillBg else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = city.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = city.province,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Terpilih",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
