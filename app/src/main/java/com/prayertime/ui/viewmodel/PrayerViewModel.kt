package com.prayertime.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prayertime.data.datastore.PrayerPreferences
import com.prayertime.data.location.CityPreset
import com.prayertime.data.location.IndonesiaDistricts
import com.prayertime.data.location.LocationManager
import com.prayertime.data.repository.CalculationMethodType
import com.prayertime.data.repository.DayPrayerSchedule
import com.prayertime.data.repository.PrayerRepository
import com.prayertime.notification.PrayerAlarmScheduler
import com.prayertime.ui.model.NavigationItem
import com.prayertime.ui.model.PrayerItemUiModel
import com.prayertime.ui.model.PrayerUiState
import com.prayertime.widget.PrayerWidgetScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

/**
 * [AndroidViewModel] pusat pengendali logika bisnis dan status UI aplikasi Jadwal Shalat.
 * Bertanggung jawab atas:
 * - Menyajikan [StateFlow] reaktif untuk dashboard ([PrayerUiState]), dialog bottom sheet, dan pencarian kota.
 * - Memperbarui linimasa shalat secara berkala melalui coroutine ticker hemat baterai (tiap 30 detik).
 * - Menangani deteksi GPS otomatis, pencarian instan pada dataset 514 Kabupaten/Kota, dan pemilihan metode hisab.
 * - Mengatur persistensi preferensi ke Jetpack DataStore dan sinkronisasi alarm pada [android.app.AlarmManager].
 */
class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerRepository()
    private val preferences = PrayerPreferences(application)

    /** Status UI utama layar dashboard jadwal shalat */
    private val _uiState = MutableStateFlow(repository.getFullPrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    /** Status pemuatan koordinat via GPS */
    private val _isGpsLoading = MutableStateFlow(false)
    val isGpsLoading: StateFlow<Boolean> = _isGpsLoading.asStateFlow()

    /** Status proses pencarian kota / Geocoder */
    private val _isSearchingCities = MutableStateFlow(false)
    val isSearchingCities: StateFlow<Boolean> = _isSearchingCities.asStateFlow()

    /** Hasil daftar kota yang cocok dengan query pencarian */
    private val _searchResults = MutableStateFlow<List<CityPreset>>(IndonesiaDistricts.ALL_514_CITIES)
    val searchResults: StateFlow<List<CityPreset>> = _searchResults.asStateFlow()

    /** Kontrol visibilitas bottom sheet pemilihan kota */
    private val _showLocationSheet = MutableStateFlow(false)
    val showLocationSheet: StateFlow<Boolean> = _showLocationSheet.asStateFlow()
    val isLocationSheetVisible: StateFlow<Boolean> = showLocationSheet

    /** Kontrol visibilitas bottom sheet pemilihan metode hisab */
    private val _showMethodSheet = MutableStateFlow(false)
    val showMethodSheet: StateFlow<Boolean> = _showMethodSheet.asStateFlow()
    val isCalculationMethodSheetVisible: StateFlow<Boolean> = showMethodSheet

    /** Kontrol visibilitas bottom sheet jadwal shalat 7 hari */
    private val _showWeeklySheet = MutableStateFlow(false)
    val showWeeklySheet: StateFlow<Boolean> = _showWeeklySheet.asStateFlow()
    val isWeeklySheetVisible: StateFlow<Boolean> = showWeeklySheet

    /** Kontrol visibilitas bottom sheet tasbih digital */
    private val _showTasbihSheet = MutableStateFlow(false)
    val showTasbihSheet: StateFlow<Boolean> = _showTasbihSheet.asStateFlow()
    val isTasbihSheetVisible: StateFlow<Boolean> = showTasbihSheet

    /** Data jadwal shalat 7 hari ke depan */
    private val _weeklySchedule = MutableStateFlow<List<DayPrayerSchedule>>(emptyList())
    val weeklySchedule: StateFlow<List<DayPrayerSchedule>> = _weeklySchedule.asStateFlow()

    /** Metode hisab yang sedang aktif */
    private val _currentMethod = MutableStateFlow(CalculationMethodType.KEMENAG)
    val currentMethod: StateFlow<CalculationMethodType> = _currentMethod.asStateFlow()

    /** Peta status aktif lonceng notifikasi untuk masing-masing waktu shalat */
    private val _notificationSettings = MutableStateFlow<Map<String, Boolean>>(
        mapOf(
            "subuh" to true,
            "dzuhur" to true,
            "ashar" to true,
            "maghrib" to true,
            "isya" to true
        )
    )
    val notificationSettings: StateFlow<Map<String, Boolean>> = _notificationSettings.asStateFlow()

    // Koordinat Aktif
    var currentLatitude: Double = PrayerRepository.DEFAULT_LATITUDE
        private set
    var currentLongitude: Double = PrayerRepository.DEFAULT_LONGITUDE
        private set
    var currentLocationName: String = PrayerRepository.DEFAULT_LOCATION_NAME
        private set

    private var tickerJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadSavedPreferences()
        startRealtimeTicker()
    }

    private fun loadSavedPreferences() {
        viewModelScope.launch {
            val savedLocation = preferences.getSavedLocationDirect()
            val savedMethodId = preferences.getSavedMethodDirect()
            val savedNotifMap = preferences.getNotificationSettingsDirect()

            currentLatitude = savedLocation.latitude
            currentLongitude = savedLocation.longitude
            currentLocationName = savedLocation.cityName
            _currentMethod.value = CalculationMethodType.fromId(savedMethodId)
            _notificationSettings.value = savedNotifMap

            refreshPrayerTimes()

            // Daftarkan/sinkronkan seluruh alarm shalat di AlarmManager
            PrayerAlarmScheduler.rescheduleAllAlarms(getApplication())
        }
    }

    private fun startRealtimeTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                refreshPrayerTimes()
                delay(30_000L) // Refresh tiap 30 detik untuk menghemat baterai
            }
        }
    }

    /**
     * Memperbarui jadwal shalat, hitung mundur, dan status aktif secara real-time
     * berdasarkan koordinat aktif dan waktu saat ini.
     */
    fun refreshPrayerTimes() {
        val now = Date()
        val updatedState = repository.getFullPrayerUiState(
            latitude = currentLatitude,
            longitude = currentLongitude,
            locationName = currentLocationName,
            method = _currentMethod.value,
            now = now
        )

        _uiState.update { current ->
            updatedState.copy(
                currentSelectedTab = current.currentSelectedTab,
                prayerTimeline = updatedState.prayerTimeline.map { newItem ->
                    val isEnabled = _notificationSettings.value[newItem.id] ?: true
                    newItem.copy(isNotificationEnabled = isEnabled)
                }
            )
        }

        // Update jadwal mingguan jika sheet sedang terbuka
        _weeklySchedule.value = repository.getWeeklySchedule(
            latitude = currentLatitude,
            longitude = currentLongitude,
            method = _currentMethod.value,
            startDate = now
        )
    }

    /** Membuka bottom sheet pemilihan lokasi dan mereset hasil pencarian ke daftar 514 kota */
    fun openLocationSheet() {
        _searchResults.value = IndonesiaDistricts.ALL_514_CITIES
        _showLocationSheet.value = true
    }
    /** Menutup bottom sheet pemilihan lokasi */
    fun closeLocationSheet() { _showLocationSheet.value = false }

    /** Membuka bottom sheet pemilihan metode hisab */
    fun openMethodSheet() { _showMethodSheet.value = true }
    /** Menutup bottom sheet pemilihan metode hisab */
    fun closeMethodSheet() { _showMethodSheet.value = false }

    /** Membuka bottom sheet jadwal shalat 7 hari (Pekanan) */
    fun openWeeklySheet() {
        _weeklySchedule.value = repository.getWeeklySchedule(
            latitude = currentLatitude,
            longitude = currentLongitude,
            method = _currentMethod.value
        )
        _showWeeklySheet.value = true
    }
    /** Menutup bottom sheet jadwal shalat 7 hari */
    fun closeWeeklySheet() { _showWeeklySheet.value = false }

    /** Membuka bottom sheet tasbih digital interaktif */
    fun openTasbihSheet() { _showTasbihSheet.value = true }
    /** Menutup bottom sheet tasbih digital */
    fun closeTasbihSheet() { _showTasbihSheet.value = false }

    /**
     * Melakukan pencarian kota atau kabupaten secara dinamis.
     * Mengutamakan pencarian instan pada dataset lokal 514 kota, dengan fallback ke Geocoder.
     *
     * @param query Teks pencarian nama daerah atau kota.
     */
    fun searchCities(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearchingCities.value = true
            val results = LocationManager.searchLocations(getApplication(), query)
            _searchResults.value = results
            _isSearchingCities.value = false
        }
    }

    /**
     * Meminta koordinat GPS perangkat saat ini secara otomatis, melakukan reverse-geocoding,
     * menyimpan ke DataStore, dan menyinkronkan seluruh widget serta jadwal alarm.
     *
     * @param context Konteks aplikasi.
     */
    fun requestGpsLocation(context: Context) {
        viewModelScope.launch {
            _isGpsLoading.value = true
            try {
                val coordinates = LocationManager.getDeviceLocation(context)
                if (coordinates != null) {
                    val (lat, lng) = coordinates
                    val cityName = LocationManager.getCityName(context, lat, lng)
                    val fullLocationName = "$cityName, Indonesia"

                    currentLatitude = lat
                    currentLongitude = lng
                    currentLocationName = fullLocationName

                    preferences.saveLocation(lat, lng, fullLocationName, isGpsAuto = true)
                    refreshPrayerTimes()
                    PrayerWidgetScheduler.updateAllWidgets(context)
                    PrayerAlarmScheduler.rescheduleAllAlarms(context)
                    closeLocationSheet()
                }
            } finally {
                _isGpsLoading.value = false
            }
        }
    }

    /**
     * Memilih kota secara manual dari daftar preset, memperbarui jadwal shalat,
     * menyimpan ke DataStore, dan menyinkronkan widget serta alarm.
     *
     * @param preset Data kota pilihan ([CityPreset]).
     * @param context Konteks aplikasi.
     */
    fun selectCity(preset: CityPreset, context: Context) {
        viewModelScope.launch {
            val fullLocationName = "${preset.name}, Indonesia"
            currentLatitude = preset.latitude
            currentLongitude = preset.longitude
            currentLocationName = fullLocationName

            preferences.saveLocation(preset.latitude, preset.longitude, fullLocationName, isGpsAuto = false)
            refreshPrayerTimes()
            PrayerWidgetScheduler.updateAllWidgets(context)
            PrayerAlarmScheduler.rescheduleAllAlarms(context)
            closeLocationSheet()
        }
    }

    /**
     * Mengganti metode hisab astronomis, memperbarui jadwal shalat seketika,
     * dan menyinkronkan widget serta alarm.
     *
     * @param method Opsi metode hisab terpilih ([CalculationMethodType]).
     * @param context Konteks aplikasi.
     */
    fun selectCalculationMethod(method: CalculationMethodType, context: Context) {
        viewModelScope.launch {
            _currentMethod.value = method
            preferences.saveCalculationMethod(method.id)
            refreshPrayerTimes()
            PrayerWidgetScheduler.updateAllWidgets(context)
            PrayerAlarmScheduler.rescheduleAllAlarms(context)
            closeMethodSheet()
        }
    }

    /**
     * Mengubah status lonceng notifikasi (aktif/hening) untuk waktu shalat tertentu,
     * menyimpan status ke DataStore, dan mendaftarkan/membatalkan alarm di [android.app.AlarmManager].
     *
     * @param prayerItem Item shalat yang di-toggle ([PrayerItemUiModel]).
     * @param context Konteks aplikasi.
     */
    fun toggleNotification(prayerItem: PrayerItemUiModel, context: Context) {
        viewModelScope.launch {
            val newStatus = !prayerItem.isNotificationEnabled
            preferences.toggleNotification(prayerItem.id, newStatus)

            _notificationSettings.update { current ->
                current + (prayerItem.id to newStatus)
            }

            _uiState.update { state ->
                val updated = state.prayerTimeline.map {
                    if (it.id == prayerItem.id) it.copy(isNotificationEnabled = newStatus) else it
                }
                state.copy(prayerTimeline = updated)
            }

            // Atur AlarmManager seketika (jadwalkan alarm jika aktif, batalkan jika hening)
            if (newStatus) {
                PrayerAlarmScheduler.rescheduleAllAlarms(context)
            } else {
                PrayerAlarmScheduler.cancelSingleAlarm(context, prayerItem.id)
            }

            // Tampilkan konfirmasi Toast kepada pengguna
            val message = if (newStatus) {
                "Pengingat ${prayerItem.name} diaktifkan"
            } else {
                "Pengingat ${prayerItem.name} dinonaktifkan"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Memperbarui tab yang sedang dipilih pada model UI.
     */
    fun onTabSelected(tab: NavigationItem) {
        _uiState.update { it.copy(currentSelectedTab = tab) }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        searchJob?.cancel()
    }
}
