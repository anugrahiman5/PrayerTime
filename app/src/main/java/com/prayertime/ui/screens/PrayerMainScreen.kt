package com.prayertime.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prayertime.ui.components.*
import com.prayertime.ui.model.PrayerItemUiModel
import com.prayertime.ui.model.PrayerUiState
import com.prayertime.ui.model.createDummyPrayerUiState
import com.prayertime.ui.theme.PrayerTimeTheme
import com.prayertime.ui.viewmodel.PrayerViewModel

/**
 * Stateful Screen yang menghubungkan UI Layar Utama dengan ViewModel.
 * Seluruh BottomSheet telah di-hoist ke level root (MainActivity) sehingga
 * interaksi tetap responsif dari tab manapun tanpa keterlambatan.
 */
@Composable
fun PrayerMainScreen(
    viewModel: PrayerViewModel,
    onNavigateToQibla: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    PrayerMainScreenContent(
        uiState = uiState,
        modifier = modifier,
        onLocationClick = { viewModel.openLocationSheet() },
        onFilterClick = { viewModel.openMethodSheet() },
        onSettingsClick = onNavigateToSettings,
        onMethodClick = { viewModel.openMethodSheet() },
        onSemingguClick = { viewModel.openWeeklySheet() },
        onTasbihClick = { viewModel.openTasbihSheet() },
        onQiblaClick = onNavigateToQibla,
        onNotificationToggle = { viewModel.toggleNotification(it, context) }
    )
}

/**
 * Composable murni (Stateless) penyusun tata letak visual layar utama jadwal shalat.
 * Terdiri dari:
 * 1. [TopLocationBar]: Header tanggal Hijriah/Masehi, nama kota, dan tombol aksi filter/pengaturan.
 * 2. [HeroPrayerCard]: Kartu utama waktu shalat terdekat berikutnya dengan hitung mundur dan bilah progres.
 * 3. [QuickInfoCards]: Kartu akses cepat kompas arah kiblat dan informasi metode hisab.
 * 4. [PrayerTimelineList]: Daftar linimasa 6 waktu shalat hari ini beserta tombol jadwal 7 hari.
 * 5. [TasbihQuickCard]: Kartu peluncur fitur tasbih digital interaktif.
 */
@Composable
fun PrayerMainScreenContent(
    uiState: PrayerUiState,
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSemingguClick: () -> Unit = {},
    onPrayerClick: (PrayerItemUiModel) -> Unit = {},
    onNotificationToggle: (PrayerItemUiModel) -> Unit = {},
    onQiblaClick: () -> Unit = {},
    onMethodClick: () -> Unit = {},
    onTasbihClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // 1. Top Location & Date Bar
            TopLocationBar(
                hijriDate = uiState.hijriDate,
                gregorianDate = uiState.gregorianDate,
                locationName = uiState.locationName,
                accuracyTag = uiState.accuracyTag,
                onLocationClick = onLocationClick,
                onFilterClick = onFilterClick,
                onSettingsClick = onSettingsClick
            )

            // 2. Hero Prayer Card
            HeroPrayerCard(
                model = uiState.heroPrayer
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Quick Info Cards (Arah Kiblat & Metode Hisab)
            QuickInfoCards(
                qiblaInfo = uiState.qiblaInfo,
                methodInfo = uiState.calculationMethod,
                onQiblaClick = onQiblaClick,
                onMethodClick = onMethodClick
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Prayer Timeline List (Jadwal 6 Waktu & Seminggu)
            PrayerTimelineList(
                prayers = uiState.prayerTimeline,
                onSemingguClick = onSemingguClick,
                onPrayerClick = onPrayerClick,
                onNotificationToggle = onNotificationToggle
            )

            // 5. Quick Tasbih Digital Card
            TasbihQuickCard(
                tasbihInfo = uiState.tasbihQuickAction,
                onStartClick = onTasbihClick
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

// Previews
@Preview(name = "Prayer Main Screen - Light", showBackground = true, backgroundColor = 0xFFF5F8F5)
@Composable
fun PreviewPrayerMainScreenLight() {
    PrayerTimeTheme(darkTheme = false) {
        Surface {
            PrayerMainScreenContent(uiState = createDummyPrayerUiState())
        }
    }
}
