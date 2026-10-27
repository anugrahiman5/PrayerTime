package com.prayertime

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prayertime.data.location.LocationManager
import com.prayertime.notification.PrayerAlarmScheduler
import com.prayertime.notification.PrayerNotificationHelper
import com.prayertime.ui.components.BottomQuickActions
import com.prayertime.ui.components.CalculationMethodBottomSheet
import com.prayertime.ui.components.LocationSelectionBottomSheet
import com.prayertime.ui.components.TasbihModalSheet
import com.prayertime.ui.components.WeeklyScheduleBottomSheet
import com.prayertime.ui.model.NavigationItem
import com.prayertime.ui.screens.DoaScreen
import com.prayertime.ui.screens.PrayerMainScreen
import com.prayertime.ui.screens.QiblaCompassScreen
import com.prayertime.ui.screens.SettingsScreen
import com.prayertime.ui.theme.PrayerTimeTheme
import com.prayertime.ui.viewmodel.PrayerViewModel
import com.prayertime.widget.PrayerWidgetScheduler

/**
 * Activity tunggal (Single-Activity Architecture) sebagai titik masuk utama aplikasi Jadwal Shalat.
 * Bertanggung jawab menginisialisasi:
 * - [enableEdgeToEdge] untuk layout modern layar penuh.
 * - [PrayerNotificationHelper.createNotificationChannel] untuk saluran pengingat shalat.
 * - Sinkronisasi jadwal [android.app.AlarmManager] dan pembaruan Glance AppWidget.
 * - Memasang tema [PrayerTimeTheme] dan grafik navigasi utama [MainAppNavigation].
 */
class MainActivity : ComponentActivity() {

    private val prayerViewModel: PrayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi Notification Channel & Sinkronisasi Alarm
        PrayerNotificationHelper.createNotificationChannel(this)
        PrayerAlarmScheduler.rescheduleAllAlarms(this)
        PrayerWidgetScheduler.scheduleNextPrayerFromNow(this)

        setContent {
            PrayerTimeTheme {
                MainAppNavigation(viewModel = prayerViewModel)
            }
        }
    }
}

/**
 * Komponen navigasi tingkat atas (Root Composable) yang menghubungkan:
 * 1. [NavHost] dengan 4 rute utama: "shalat", "kiblat", "doa", "pengaturan".
 * 2. Bottom Navigation Bar interaktif dengan state backstack yang tersinkronisasi.
 * 3. Hoisted Modal Bottom Sheets (Lokasi, Metode Hisab, Pekanan, Tasbih) di level root
 *    sehingga dapat diakses secara instan dari layar manapun tanpa tertunda.
 * 4. Pengecekan izin runtime lokasi (GPS) dan notifikasi Android 13+ (POST_NOTIFICATIONS).
 *
 * @param viewModel Instans [PrayerViewModel] induk untuk mengelola state global.
 */
@Composable
fun MainAppNavigation(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "shalat"

    val uiState by viewModel.uiState.collectAsState()
    val currentMethod by viewModel.currentMethod.collectAsState()
    val isGpsLoading by viewModel.isGpsLoading.collectAsState()
    val isSearchingCities by viewModel.isSearchingCities.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    // State visibilitas bottom sheet yang di-hoist ke level root
    val isLocationSheetVisible by viewModel.isLocationSheetVisible.collectAsState()
    val isCalculationMethodSheetVisible by viewModel.isCalculationMethodSheetVisible.collectAsState()
    val isWeeklySheetVisible by viewModel.isWeeklySheetVisible.collectAsState()
    val isTasbihSheetVisible by viewModel.isTasbihSheetVisible.collectAsState()
    val weeklySchedule by viewModel.weeklySchedule.collectAsState()

    // Permission Launcher untuk GPS Lokasi di root level
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.requestGpsLocation(context)
        }
    }

    // Permission Launcher untuk Notifikasi di Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            PrayerAlarmScheduler.rescheduleAllAlarms(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val selectedNavItem = when (currentRoute) {
        "shalat" -> NavigationItem.SHALAT
        "kiblat" -> NavigationItem.KIBLAT
        "doa" -> NavigationItem.DOA
        "pengaturan" -> NavigationItem.PENGATURAN
        else -> NavigationItem.SHALAT
    }

    // Fungsi navigasi terpadu untuk bottom bar dan aksi antar layar
    val navigateToTab: (String) -> Unit = { destinationRoute ->
        if (currentRoute != destinationRoute) {
            if (destinationRoute == "shalat") {
                // Untuk kembali ke layar Shalat (start destination), pop semua stack di atasnya
                val popped = navController.popBackStack("shalat", inclusive = false)
                if (!popped) {
                    navController.navigate("shalat") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                navController.navigate(destinationRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomQuickActions(
                selectedItem = selectedNavItem,
                onItemSelected = { item ->
                    val destinationRoute = when (item) {
                        NavigationItem.SHALAT -> "shalat"
                        NavigationItem.KIBLAT -> "kiblat"
                        NavigationItem.DOA -> "doa"
                        NavigationItem.PENGATURAN -> "pengaturan"
                    }
                    navigateToTab(destinationRoute)
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "shalat",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Layar Utama Jadwal Shalat
            composable("shalat") {
                PrayerMainScreen(
                    viewModel = viewModel,
                    onNavigateToQibla = {
                        navigateToTab("kiblat")
                    },
                    onNavigateToSettings = {
                        navigateToTab("pengaturan")
                    }
                )
            }

            // 2. Layar Kompas Arah Kiblat Interaktif
            composable("kiblat") {
                QiblaCompassScreen(
                    onNavigateBack = {
                        if (!navController.popBackStack()) {
                            navigateToTab("shalat")
                        }
                    },
                    latitude = viewModel.currentLatitude,
                    longitude = viewModel.currentLongitude,
                    locationName = viewModel.currentLocationName
                )
            }

            // 3. Layar Kumpulan Doa Harian
            composable("doa") {
                DoaScreen()
            }

            // 4. Layar Pengaturan & Informasi
            composable("pengaturan") {
                SettingsScreen(
                    currentLocationName = viewModel.currentLocationName,
                    currentMethodName = currentMethod.title,
                    onOpenLocationSheet = { viewModel.openLocationSheet() },
                    onOpenMethodSheet = { viewModel.openMethodSheet() }
                )
            }
        }
    }

    // =========================================================================
    // Hoisted Modal Bottom Sheets di level root (sejajar dengan NavHost)
    // Terbuka instan tanpa tertunda baik dari tab Shalat, Pengaturan, atau Layar manapun
    // =========================================================================

    // 1. Bottom Sheet Pemilihan Lokasi & Pencarian Cepat 514 Kab/Kota
    if (isLocationSheetVisible) {
        LocationSelectionBottomSheet(
            currentCityName = uiState.locationName,
            isLoadingGps = isGpsLoading,
            isSearching = isSearchingCities,
            citiesList = searchResults,
            onDismissRequest = { viewModel.closeLocationSheet() },
            onUseGpsClick = {
                if (LocationManager.hasLocationPermission(context)) {
                    viewModel.requestGpsLocation(context)
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            onSearchQueryChanged = { query ->
                viewModel.searchCities(query)
            },
            onCitySelected = { city ->
                viewModel.selectCity(city, context)
            }
        )
    }

    // 2. Bottom Sheet Pemilihan Parameter Metode Hisab
    if (isCalculationMethodSheetVisible) {
        CalculationMethodBottomSheet(
            currentMethod = currentMethod,
            onDismissRequest = { viewModel.closeMethodSheet() },
            onMethodSelected = { method ->
                viewModel.selectCalculationMethod(method, context)
            }
        )
    }

    // 3. Bottom Sheet Jadwal Shalat 7 Hari (Pekanan)
    if (isWeeklySheetVisible) {
        WeeklyScheduleBottomSheet(
            weeklyList = weeklySchedule,
            cityName = uiState.locationName,
            onDismissRequest = { viewModel.closeWeeklySheet() }
        )
    }

    // 4. Bottom Sheet Tasbih Digital
    if (isTasbihSheetVisible) {
        TasbihModalSheet(
            onDismissRequest = { viewModel.closeTasbihSheet() }
        )
    }
}
