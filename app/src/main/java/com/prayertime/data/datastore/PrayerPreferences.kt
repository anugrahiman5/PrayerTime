package com.prayertime.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ============================================================================
// DataStore Preferences untuk Pengaturan Lokasi, Notifikasi & Metode Hisab
// ============================================================================

/**
 * Ekstensi context untuk instansiasi singleton DataStore Preferences dengan nama "prayer_settings".
 */
val Context.prayerDataStore: DataStore<Preferences> by preferencesDataStore(name = "prayer_settings")

/**
 * Model representasi data lokasi yang tersimpan secara lokal di DataStore.
 *
 * @property latitude Garis lintang koordinat lokasi.
 * @property longitude Garis bujur koordinat lokasi.
 * @property cityName Nama kota/daerah tampilan (misal: "Jakarta, Indonesia").
 * @property isGpsAuto Menandakan apakah lokasi didapat secara otomatis via GPS (true) atau manual (false).
 */
data class SavedLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val isGpsAuto: Boolean
)

/**
 * Manajer persistensi data preferensi aplikasi jadwal shalat menggunakan Jetpack DataStore Preferences.
 * Mengelola penyimpanan koordinat lokasi, pilihan otoritas hisab, serta status lonceng notifikasi per shalat.
 *
 * @param context Konteks aplikasi Android.
 */
class PrayerPreferences(private val context: Context) {

    companion object {
        val KEY_LATITUDE = doublePreferencesKey("latitude")
        val KEY_LONGITUDE = doublePreferencesKey("longitude")
        val KEY_CITY_NAME = stringPreferencesKey("city_name")
        val KEY_IS_GPS_AUTO = booleanPreferencesKey("is_gps_auto")
        val KEY_CALC_METHOD = stringPreferencesKey("calculation_method")

        /** Koordinat default lintang: Jakarta (-6.2088) */
        const val DEFAULT_LAT = -6.2088
        /** Koordinat default bujur: Jakarta (106.8456) */
        const val DEFAULT_LNG = 106.8456
        /** Nama lokasi default */
        const val DEFAULT_CITY = "Jakarta, Indonesia"
        /** Metode hisab standar nasional Indonesia */
        const val DEFAULT_METHOD = "KEMENAG"
    }

    /**
     * Aliran [Flow] reaktif untuk mengamati perubahan lokasi dan koordinat pengguna secara real-time.
     */
    val locationFlow: Flow<SavedLocation> = context.prayerDataStore.data.map { preferences ->
        SavedLocation(
            latitude = preferences[KEY_LATITUDE] ?: DEFAULT_LAT,
            longitude = preferences[KEY_LONGITUDE] ?: DEFAULT_LNG,
            cityName = preferences[KEY_CITY_NAME] ?: DEFAULT_CITY,
            isGpsAuto = preferences[KEY_IS_GPS_AUTO] ?: false
        )
    }

    /**
     * Aliran [Flow] reaktif untuk mengamati perubahan metode hisab aktif.
     */
    val calculationMethodFlow: Flow<String> = context.prayerDataStore.data.map { preferences ->
        preferences[KEY_CALC_METHOD] ?: DEFAULT_METHOD
    }

    /**
     * Menyimpan koordinat dan nama lokasi pengguna ke DataStore.
     */
    suspend fun saveLocation(latitude: Double, longitude: Double, cityName: String, isGpsAuto: Boolean) {
        context.prayerDataStore.edit { preferences ->
            preferences[KEY_LATITUDE] = latitude
            preferences[KEY_LONGITUDE] = longitude
            preferences[KEY_CITY_NAME] = cityName
            preferences[KEY_IS_GPS_AUTO] = isGpsAuto
        }
    }

    /**
     * Menyimpan ID metode hisab yang dipilih pengguna.
     */
    suspend fun saveCalculationMethod(methodKey: String) {
        context.prayerDataStore.edit { preferences ->
            preferences[KEY_CALC_METHOD] = methodKey
        }
    }

    /**
     * Mengaktifkan atau menonaktifkan notifikasi pengingat untuk waktu shalat tertentu.
     *
     * @param prayerId Identifier shalat ("subuh", "dzuhur", "ashar", "maghrib", "isya").
     * @param isEnabled Status lonceng notifikasi aktif atau mati.
     */
    suspend fun toggleNotification(prayerId: String, isEnabled: Boolean) {
        val key = booleanPreferencesKey("notif_$prayerId")
        context.prayerDataStore.edit { preferences ->
            preferences[key] = isEnabled
        }
    }

    /**
     * Mengamati status notifikasi untuk waktu shalat tertentu sebagai [Flow].
     */
    fun isNotificationEnabledFlow(prayerId: String): Flow<Boolean> {
        val key = booleanPreferencesKey("notif_$prayerId")
        return context.prayerDataStore.data.map { preferences ->
            preferences[key] ?: true
        }
    }

    /**
     * Mengamati seluruh status notifikasi 5 waktu shalat fardhu dalam bentuk Map.
     */
    val allNotificationSettingsFlow: Flow<Map<String, Boolean>> = context.prayerDataStore.data.map { preferences ->
        val prayerIds = listOf("subuh", "dzuhur", "ashar", "maghrib", "isya")
        prayerIds.associateWith { id ->
            preferences[booleanPreferencesKey("notif_$id")] ?: true
        }
    }

    /**
     * Membaca pengaturan notifikasi saat ini secara sinkron / langsung (one-shot).
     */
    suspend fun getNotificationSettingsDirect(): Map<String, Boolean> {
        return allNotificationSettingsFlow.first()
    }

    /**
     * Membaca data lokasi tersimpan saat ini secara langsung (one-shot) untuk kebutuhan inisialisasi / widget.
     */
    suspend fun getSavedLocationDirect(): SavedLocation {
        return locationFlow.first()
    }

    /**
     * Membaca ID metode hisab tersimpan saat ini secara langsung (one-shot).
     */
    suspend fun getSavedMethodDirect(): String {
        return calculationMethodFlow.first()
    }
}
