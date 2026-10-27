package com.prayertime.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Model data kota atau kabupaten di Indonesia.
 *
 * @property id Identifier unik daerah.
 * @property name Nama kota atau kabupaten (misal: "Kota Bandung", "Kab. Sleman").
 * @property province Nama provinsi induk.
 * @property latitude Titik koordinat garis lintang.
 * @property longitude Titik koordinat garis bujur.
 */
data class CityPreset(
    val id: String,
    val name: String,
    val province: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Manajer lokasi terpadu yang menangani:
 * 1. Pengecekan izin lokasi runtime (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION).
 * 2. Akuisisi koordinat perangkat saat ini menggunakan [LocationServices.getFusedLocationProviderClient].
 * 3. Reverse-geocoding koordinat GPS menjadi nama kota/kabupaten.
 * 4. Pencarian instan cepat (luring / offline) pada dataset 514 Kabupaten/Kota se-Indonesia.
 * 5. Fallback pencarian daring menggunakan [Geocoder] Android jika kata kunci berupa kecamatan/desa spesifik.
 */
object LocationManager {

    /**
     * Fallback Lengkap 38 Ibu Kota Provinsi di Seluruh Indonesia
     */
    val PROVINSI_CAPITALS_38 = listOf(
        // Pulau Sumatera
        CityPreset("ach", "Banda Aceh", "Aceh", 5.5483, 95.3238),
        CityPreset("mdn", "Medan", "Sumatera Utara", 3.5952, 98.6722),
        CityPreset("pdg", "Padang", "Sumatera Barat", -0.9471, 100.4172),
        CityPreset("pku", "Pekanbaru", "Riau", 0.5071, 101.4478),
        CityPreset("tjp", "Tanjung Pinang", "Kepulauan Riau", 0.9167, 104.4500),
        CityPreset("jmb", "Jambi", "Jambi", -1.6101, 103.6131),
        CityPreset("plb", "Palembang", "Sumatera Selatan", -2.9761, 104.7754),
        CityPreset("pgp", "Pangkal Pinang", "Bangka Belitung", -2.1333, 106.1167),
        CityPreset("bkl", "Bengkulu", "Bengkulu", -3.8004, 102.2655),
        CityPreset("bdl", "Bandar Lampung", "Lampung", -5.4500, 105.2667),

        // Pulau Jawa
        CityPreset("jkt", "Jakarta", "DKI Jakarta", -6.2088, 106.8456),
        CityPreset("srn", "Serang", "Banten", -6.1104, 106.1640),
        CityPreset("bdg", "Bandung", "Jawa Barat", -6.9175, 107.6191),
        CityPreset("smg", "Semarang", "Jawa Tengah", -6.9667, 110.4167),
        CityPreset("ygy", "Yogyakarta", "DI Yogyakarta", -7.7956, 110.3695),
        CityPreset("sby", "Surabaya", "Jawa Timur", -7.2575, 112.7521),

        // Bali & Nusa Tenggara
        CityPreset("dps", "Denpasar", "Bali", -8.6705, 115.2126),
        CityPreset("mtr", "Mataram", "Nusa Tenggara Barat", -8.5833, 116.1167),
        CityPreset("kpg", "Kupang", "Nusa Tenggara Timur", -10.1772, 123.6070),

        // Pulau Kalimantan
        CityPreset("ptk", "Pontianak", "Kalimantan Barat", -0.0263, 109.3425),
        CityPreset("pkr", "Palangka Raya", "Kalimantan Tengah", -2.2161, 113.9140),
        CityPreset("bjm", "Banjarmasin", "Kalimantan Selatan", -3.3194, 114.5908),
        CityPreset("smd", "Samarinda", "Kalimantan Timur", -0.5022, 117.1536),
        CityPreset("tjs", "Tanjung Selor", "Kalimantan Utara", 2.8375, 117.3653),

        // Pulau Sulawesi
        CityPreset("mdo", "Manado", "Sulawesi Utara", 1.4748, 124.8428),
        CityPreset("gtl", "Gorontalo", "Gorontalo", 0.5435, 123.0568),
        CityPreset("pal", "Palu", "Sulawesi Tengah", -0.9003, 119.8780),
        CityPreset("mmj", "Mamuju", "Sulawesi Barat", -2.6775, 118.8894),
        CityPreset("mks", "Makassar", "Sulawesi Selatan", -5.1477, 119.4327),
        CityPreset("kdr", "Kendari", "Sulawesi Tenggara", -3.9985, 122.5126),

        // Kepulauan Maluku
        CityPreset("amb", "Ambon", "Maluku", -3.6554, 128.1908),
        CityPreset("sff", "Sofifi", "Maluku Utara", 0.7417, 127.5681),

        // Pulau Papua (6 Provinsi Baru & DOB)
        CityPreset("jpr", "Jayapura", "Papua", -2.5916, 140.6690),
        CityPreset("mnk", "Manokwari", "Papua Barat", -0.8615, 134.0620),
        CityPreset("srg", "Sorong", "Papua Barat Daya", -0.8762, 131.2558),
        CityPreset("nbr", "Nabire", "Papua Tengah", -3.3667, 135.4833),
        CityPreset("wmn", "Wamena", "Papua Pegunungan", -4.0983, 138.9439),
        CityPreset("mrk", "Merauke", "Papua Selatan", -8.4991, 140.4017),

        // Kota-Kota Besar Tambahan yang Populer
        CityPreset("bpn", "Balikpapan", "Kalimantan Timur", -1.2379, 116.8529),
        CityPreset("btm", "Batam", "Kepulauan Riau", 1.0456, 104.0305),
        CityPreset("mlg", "Malang", "Jawa Timur", -7.9666, 112.6326),
        CityPreset("slo", "Surakarta (Solo)", "Jawa Tengah", -7.5755, 110.8243),
        CityPreset("bgr", "Bogor", "Jawa Barat", -6.5971, 106.8060),
        CityPreset("dpk", "Depok", "Jawa Barat", -6.4025, 106.7942),
        CityPreset("tgr", "Tangerang", "Banten", -6.1783, 106.6319),
        CityPreset("bks", "Bekasi", "Jawa Barat", -6.2383, 106.9756),
        CityPreset("crb", "Cirebon", "Jawa Barat", -6.7320, 108.5523)
    )

    /**
     * Memeriksa apakah aplikasi memiliki izin lokasi (Fine atau Coarse Location).
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Mengambil koordinat perangkat saat ini (Latitude, Longitude) menggunakan Google Play Services Fused Location.
     * Mengutamakan akurasi tinggi dan secara aman beralih ke lokasi terakhir (last known location) jika sinyal GPS lemah.
     *
     * @return [Pair] lintang dan bujur jika berhasil, atau null jika izin tidak diberikan / lokasi tidak tersedia.
     */
    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(context: Context): Pair<Double, Double>? {
        if (!hasLocationPermission(context)) return null
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val tokenSource = CancellationTokenSource()

        return suspendCancellableCoroutine { continuation ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            continuation.resume(lastLoc?.let { Pair(it.latitude, it.longitude) })
                        }.addOnFailureListener { continuation.resume(null) }
                    }
                }.addOnFailureListener { continuation.resume(null) }

            continuation.invokeOnCancellation { tokenSource.cancel() }
        }
    }

    /**
     * Melakukan reverse-geocoding dari koordinat GPS ke nama kota/kabupaten yang mudah dibaca.
     * Jika terjadi kegagalan jaringan, akan menggunakan pendekatan pencocokan jarak terdekat ke kota preset.
     */
    suspend fun getCityName(context: Context, latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            address?.subAdminArea ?: address?.locality ?: address?.adminArea ?: findClosestPresetCity(latitude, longitude)?.name ?: "Lokasi Terdeteksi"
        } catch (e: Exception) {
            findClosestPresetCity(latitude, longitude)?.name ?: "Lokasi Terdeteksi"
        }
    }

    /**
     * Pencarian Lokasi Cepat & Lengkap Seluruh Indonesia:
     * 1. Pencarian instan (filter) pertama kali langsung ke dataset lokal 514 Kab/Kota (IndonesiaDistricts)
     *    tanpa butuh jaringan internet (offline-ready, <1ms).
     * 2. Gunakan Geocoder online hanya sebagai fallback jika nama kecamatan/kelurahan/daerah spesifik
     *    tidak ditemukan di database lokal.
     */
    suspend fun searchLocations(context: Context, query: String): List<CityPreset> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return@withContext IndonesiaDistricts.ALL_514_CITIES
        }

        // 1. Cari pertama kali di database lokal 514 Kab/Kota secara instan (Offline Search)
        val normalizedQuery = cleanQuery.lowercase()
        val localMatches = IndonesiaDistricts.ALL_514_CITIES.filter { city ->
            city.name.lowercase().contains(normalizedQuery) ||
            city.province.lowercase().contains(normalizedQuery)
        }

        if (localMatches.isNotEmpty()) {
            // Urutkan agar kota yang namanya diawali query muncul paling atas
            return@withContext localMatches.sortedWith(
                compareByDescending<CityPreset> {
                    val simplifiedName = it.name.lowercase()
                        .removePrefix("kota ")
                        .removePrefix("kab. ")
                        .trim()
                    simplifiedName.startsWith(normalizedQuery)
                }.thenBy { it.name }
            )
        }

        // 2. Fallback ke Geocoder online jika pencarian lokal 514 kab/kota tidak menemukan hasil
        val results = mutableListOf<CityPreset>()
        try {
            val geocoder = Geocoder(context, Locale("id", "ID"))
            val searchQuery = if (cleanQuery.contains("indonesia", ignoreCase = true)) cleanQuery else "$cleanQuery, Indonesia"
            val addresses: List<Address>? = geocoder.getFromLocationName(searchQuery, 8)

            if (!addresses.isNullOrEmpty()) {
                addresses.forEachIndexed { index, address ->
                    val cityName = address.subAdminArea ?: address.locality ?: address.featureName ?: cleanQuery
                    val provinceName = address.adminArea ?: address.countryName ?: "Indonesia"
                    results.add(
                        CityPreset(
                            id = "geo_${address.latitude}_${address.longitude}_$index",
                            name = cityName,
                            province = provinceName,
                            latitude = address.latitude,
                            longitude = address.longitude
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Geocoder offline atau error jaringan, abaikan
        }

        return@withContext results
    }

    /**
     * Mencari kota/kabupaten terdekat dari koordinat yang diberikan berdasarkan jarak Euclidean kuadrat sederhana.
     */
    fun findClosestPresetCity(lat: Double, lng: Double): CityPreset? {
        return IndonesiaDistricts.ALL_514_CITIES.minByOrNull {
            (it.latitude - lat) * (it.latitude - lat) + (it.longitude - lng) * (it.longitude - lng)
        }
    }
}
