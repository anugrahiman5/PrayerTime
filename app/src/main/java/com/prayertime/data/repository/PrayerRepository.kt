package com.prayertime.data.repository

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import com.prayertime.ui.model.*
import com.prayertime.widget.PrayerWidgetState
import com.prayertime.widget.UpcomingPrayerWidgetItem
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*

// ============================================================================
// Model & Repository Real-Time Adhan, Jadwal Mingguan & Perhitungan Kiblat
// ============================================================================

/**
 * Pilihan metode hisab dan otoritas penentuan sudut matahari untuk waktu shalat.
 *
 * @property id Identifier unik metode.
 * @property title Judul tampilan otoritas metode hisab.
 * @property authority Nama badan/instansi penyedia hisab.
 * @property parametersDescription Keterangan derajat sudut Subuh dan Isya.
 */
enum class CalculationMethodType(
    val id: String,
    val title: String,
    val authority: String,
    val parametersDescription: String
) {
    /** Standar Kementerian Agama Republik Indonesia (Subuh 20°, Isya 18°, +2-5m Ihtiyat) */
    KEMENAG(
        id = "KEMENAG",
        title = "Kemenag RI",
        authority = "Kemenag RI",
        parametersDescription = "Subuh 20°, Isya 18° (+2-5m Ihtiyat)"
    ),
    /** Standar Muslim World League (Liga Muslim Dunia) */
    MUSLIM_WORLD_LEAGUE(
        id = "MWL",
        title = "Muslim World League",
        authority = "MWL Global",
        parametersDescription = "Subuh 18°, Isya 17°"
    ),
    /** Standar Universitas Umm Al-Qura, Makkah Arab Saudi */
    UMM_AL_QURA(
        id = "UMM_AL_QURA",
        title = "Umm Al-Qura",
        authority = "Makkah, KSA",
        parametersDescription = "Subuh 18.5°, Isya 90m"
    ),
    /** Standar Otoritas Umum Survei Mesir */
    EGYPTIAN(
        id = "EGYPTIAN",
        title = "Egyptian Authority",
        authority = "Mesir / Afrika",
        parametersDescription = "Subuh 19.5°, Isya 17.5°"
    ),
    /** Standar Majlis Ugama Islam Singapura (MUIS) */
    SINGAPORE(
        id = "SINGAPORE",
        title = "MUIS Singapura",
        authority = "MUIS / Nusantara",
        parametersDescription = "Subuh 20°, Isya 18°"
    );

    companion object {
        /**
         * Mengonversi string ID menjadi enum [CalculationMethodType], default ke [KEMENAG].
         */
        fun fromId(id: String): CalculationMethodType {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: KEMENAG
        }
    }
}

/**
 * Informasi kalkulasi real-time waktu shalat berikutnya yang akan dimasuki.
 *
 * @property type Tipe shalat ([PrayerType]).
 * @property name Nama shalat (misal: "Ashar").
 * @property time Objek [Date] jam masuk shalat.
 * @property timeFormatted Jam shalat dalam format "HH:mm".
 * @property remainingMillis Selisih sisa waktu dalam milidetik dari sekarang.
 * @property countdownFormatted Teks hitung mundur lengkap (misal: "Tersisa 01 jam 45 menit").
 * @property countdownShort Teks hitung mundur ringkas untuk badge widget (misal: "1j 45m lagi").
 * @property countdownDetailed Teks hitung mundur jam:menit:detik (misal: "Tersisa 01:45:12").
 * @property prevStationLabel Keterangan waktu shalat sebelumnya pada bar linimasa.
 * @property currentStationLabel Keterangan stasiun waktu shalat saat ini.
 * @property nextStationLabel Keterangan waktu shalat berikutnya.
 * @property progressFraction Persentase durasi waktu yang telah berjalan antara dua shalat (0.0f - 1.0f).
 */
data class NextPrayerInfo(
    val type: PrayerType,
    val name: String,
    val time: Date,
    val timeFormatted: String,
    val remainingMillis: Long,
    val countdownFormatted: String,
    val countdownShort: String,
    val countdownDetailed: String,
    val prevStationLabel: String,
    val currentStationLabel: String,
    val nextStationLabel: String,
    val progressFraction: Float
)

/**
 * Model jadwal shalat lengkap untuk satu hari (digunakan pada tabel jadwal 7 hari / mingguan).
 *
 * @property dayName Nama hari dalam penanggalan Islam (Ahad - Sabtu).
 * @property gregorianFormatted Tanggal Masehi terformat (misal: "Ahad, 27 Okt").
 * @property hijriFormatted Tanggal Hijriah ringkas (misal: "14 Syawal").
 * @property isToday Menandakan apakah hari ini adalah tanggal hari berjalan.
 * @property subuh Jam masuk Subuh.
 * @property terbit Jam terbit matahari (Syuruq).
 * @property dzuhur Jam masuk Dzuhur.
 * @property ashar Jam masuk Ashar.
 * @property maghrib Jam masuk Maghrib.
 * @property isya Jam masuk Isya.
 */
data class DayPrayerSchedule(
    val dayName: String,
    val gregorianFormatted: String,
    val hijriFormatted: String,
    val isToday: Boolean,
    val subuh: String,
    val terbit: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String
)

/**
 * Hasil kalkulasi arah kiblat dan jarak geodesik menuju Ka'bah.
 *
 * @property degree Sudut azimuth kiblat dalam derajat (0° - 360° searah jarum jam dari Utara).
 * @property degreeFormatted Teks derajat sudut (misal: "295°").
 * @property directionText Arah mata angin terdekat dalam Bahasa Indonesia (misal: "Barat Laut").
 * @property distanceKm Jarak lengkung bumi menuju Ka'bah dalam satuan kilometer.
 * @property distanceFormatted Teks jarak yang diformat ribuan (misal: "7.925 km").
 */
data class QiblaCalculationResult(
    val degree: Double,
    val degreeFormatted: String,
    val directionText: String,
    val distanceKm: Double,
    val distanceFormatted: String
)

/**
 * Repositori utama untuk seluruh kalkulasi astronomis:
 * 1. Jadwal shalat harian & mingguan via library Adhan.
 * 2. Perhitungan arah kiblat & jarak Great Circle (Haversine) menuju Ka'bah.
 * 3. Hitung mundur waktu shalat real-time dan fraksi progres linimasa.
 * 4. Konversi penanggalan Masehi & Hijriah dengan penyesuaian nama hari Islam (Ahad - Sabtu).
 */
class PrayerRepository {

    companion object {
        const val DEFAULT_LATITUDE = -6.2088
        const val DEFAULT_LONGITUDE = 106.8456
        const val DEFAULT_LOCATION_NAME = "Jakarta, Indonesia"

        // Koordinat Ka'bah di Masjidil Haram, Makkah
        const val KAABA_LATITUDE = 21.4225
        const val KAABA_LONGITUDE = 39.8262

        private val HIJRI_MONTHS = arrayOf(
            "Muharram", "Safar", "Rabi'ul Awal", "Rabi'ul Akhir",
            "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
        )
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    private val gregorianDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    private val dayDateFormat = SimpleDateFormat("EEEE, d MMM", Locale("id", "ID"))
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))

    /**
     * Formatting nama hari Islam (mengganti 'Minggu' menjadi 'Ahad'):
     * Ahad, Senin, Selasa, Rabu, Kamis, Jumat, Sabtu
     */
    fun formatIslamicDayName(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        return if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            "Ahad"
        } else {
            dayNameFormat.format(date)
        }
    }

    /**
     * Formatting tanggal Masehi lengkap dengan override hari 'Ahad':
     * Contoh: "Ahad, 27 Oktober 2026"
     */
    fun formatIslamicGregorianDate(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        val formatted = gregorianDateFormat.format(date)
        return if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            formatted.replaceFirst("Minggu", "Ahad")
        } else {
            formatted
        }
    }

    /**
     * Formatting tanggal ringkas dengan override hari 'Ahad':
     * Contoh: "Ahad, 27 Okt"
     */
    fun formatIslamicDayDate(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        val formatted = dayDateFormat.format(date)
        return if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            formatted.replaceFirst("Minggu", "Ahad")
        } else {
            formatted
        }
    }

    /**
     * Mengambil parameter sudut hisab astronomis ([CalculationParameters]) sesuai dengan [CalculationMethodType] yang dipilih.
     * Untuk metode [CalculationMethodType.KEMENAG], ditambahkan waktu pengaman (Ihtiyat) sebesar +2 menit
     * sesuai ketetapan fatwa hisab rukyat Kementerian Agama Republik Indonesia.
     */
    fun getParametersForMethod(method: CalculationMethodType): CalculationParameters {
        return when (method) {
            CalculationMethodType.KEMENAG -> CalculationParameters(20.0, 18.0).apply {
                madhab = Madhab.SHAFI
                // Standar resmi Kementerian Agama RI (Ihtiyat pengaman waktu +2 menit)
                adjustments.fajr = 3
                adjustments.sunrise = -2 // Waktu terbit dikurangi untuk kehati-hatian batas akhir Subuh
                adjustments.dhuhr = 4    // Menjamin matahari telah tergelincir melewati meridian (ba'da zawal)
                adjustments.asr = 3
                adjustments.maghrib = 4
                adjustments.isha = 0
            }
            CalculationMethodType.MUSLIM_WORLD_LEAGUE -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters.apply {
                madhab = Madhab.SHAFI
            }
            CalculationMethodType.UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA.parameters.apply {
                madhab = Madhab.SHAFI
            }
            CalculationMethodType.EGYPTIAN -> CalculationMethod.EGYPTIAN.parameters.apply {
                madhab = Madhab.SHAFI
            }
            CalculationMethodType.SINGAPORE -> CalculationMethod.SINGAPORE.parameters.apply {
                madhab = Madhab.SHAFI
            }
        }
    }

    /**
     * Menghitung objek [PrayerTimes] untuk tanggal dan koordinat lokasi tertentu menggunakan library Adhan.
     */
    fun getPrayerTimesForDate(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        date: Date = Date(),
        method: CalculationMethodType = CalculationMethodType.KEMENAG
    ): PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents.from(date)
        val params = getParametersForMethod(method)
        return PrayerTimes(coordinates, dateComponents, params)
    }

    /**
     * Menghitung Sudut Kiblat & Jarak Geodesik (Great Circle) Menuju Ka'bah
     */
    fun calculateQibla(latitude: Double, longitude: Double): QiblaCalculationResult {
        val userLatRad = Math.toRadians(latitude)
        val userLngRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLngRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLng = kaabaLngRad - userLngRad

        // Formula Great Circle Bearing untuk Arah Kiblat
        val y = sin(deltaLng) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLng)
        var qiblaDeg = Math.toDegrees(atan2(y, x))
        qiblaDeg = (qiblaDeg + 360.0) % 360.0

        // Formula Haversine untuk Jarak ke Ka'bah (dalam Kilometer)
        val earthRadiusKm = 6371.0
        val dLat = kaabaLatRad - userLatRad
        val a = sin(dLat / 2).pow(2) + cos(userLatRad) * cos(kaabaLatRad) * sin(deltaLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = earthRadiusKm * c

        val degreeFormatted = "${Math.round(qiblaDeg)}°"
        val directionText = getCardinalDirection(qiblaDeg)
        val distanceFormatted = String.format(Locale.getDefault(), "%,.0f km", distanceKm)

        return QiblaCalculationResult(
            degree = qiblaDeg,
            degreeFormatted = degreeFormatted,
            directionText = directionText,
            distanceKm = distanceKm,
            distanceFormatted = distanceFormatted
        )
    }

    /**
     * Mengambil model representasi UI untuk kartu arah kiblat ([QiblaInfoUiModel]).
     */
    fun getQiblaInfo(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        locationName: String = DEFAULT_LOCATION_NAME
    ): QiblaInfoUiModel {
        val qiblaResult = calculateQibla(latitude, longitude)
        val cityName = locationName.split(",").firstOrNull()?.trim() ?: "Jakarta"

        return QiblaInfoUiModel(
            title = "Arah Kiblat",
            degrees = qiblaResult.degreeFormatted,
            direction = qiblaResult.directionText,
            subtitle = "Dari posisi $cityName"
        )
    }

    /**
     * Menghitung Jadwal Shalat Seminggu (7 Hari ke Depan).
     */
    fun getWeeklySchedule(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        method: CalculationMethodType = CalculationMethodType.KEMENAG,
        startDate: Date = Date()
    ): List<DayPrayerSchedule> {
        val result = mutableListOf<DayPrayerSchedule>()
        val calendar = Calendar.getInstance().apply { time = startDate }

        for (i in 0..6) {
            val currentDate = calendar.time
            val times = getPrayerTimesForDate(latitude, longitude, currentDate, method)

            val hijriFormatted = getHijriDayMonthForDate(currentDate)
            val dayName = formatIslamicDayName(currentDate)
            val gregorianFormatted = formatIslamicDayDate(currentDate)

            result.add(
                DayPrayerSchedule(
                    dayName = dayName,
                    gregorianFormatted = gregorianFormatted,
                    hijriFormatted = hijriFormatted,
                    isToday = i == 0,
                    subuh = timeFormat.format(times.fajr),
                    terbit = timeFormat.format(times.sunrise),
                    dzuhur = timeFormat.format(times.dhuhr),
                    ashar = timeFormat.format(times.asr),
                    maghrib = timeFormat.format(times.maghrib),
                    isya = timeFormat.format(times.isha)
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }

    /**
     * Menghitung rincian waktu shalat terdekat berikutnya yang akan dimasuki,
     * termasuk hitung mundur waktu, persentase progres, dan label stasiun waktu.
     */
    fun getNextPrayerInfo(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        now: Date = Date(),
        method: CalculationMethodType = CalculationMethodType.KEMENAG
    ): NextPrayerInfo {
        val todayTimes = getPrayerTimesForDate(latitude, longitude, now, method)

        val tomorrowCal = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowTimes = getPrayerTimesForDate(latitude, longitude, tomorrowCal.time, method)

        val scheduleList = listOf(
            Triple(PrayerType.SUBUH, "Subuh", todayTimes.fajr),
            Triple(PrayerType.TERBIT, "Terbit (Syuruq)", todayTimes.sunrise),
            Triple(PrayerType.DZUHUR, "Dzuhur", todayTimes.dhuhr),
            Triple(PrayerType.ASHAR, "Ashar", todayTimes.asr),
            Triple(PrayerType.MAGHRIB, "Maghrib", todayTimes.maghrib),
            Triple(PrayerType.ISYA, "Isya", todayTimes.isha)
        )

        var nextTarget = scheduleList.firstOrNull { it.third.after(now) }
        val prevStationTriple: Triple<PrayerType, String, Date>
        val currentStationLabel: String
        val nextStationLabel: String

        if (nextTarget == null) {
            nextTarget = Triple(PrayerType.SUBUH, "Subuh", tomorrowTimes.fajr)
            prevStationTriple = Triple(PrayerType.ISYA, "Isya", todayTimes.isha)
            currentStationLabel = "Sekarang: Malam Hari"
            nextStationLabel = "Subuh (${timeFormat.format(tomorrowTimes.fajr)})"
        } else {
            val targetIndex = scheduleList.indexOf(nextTarget)
            prevStationTriple = if (targetIndex > 0) {
                scheduleList[targetIndex - 1]
            } else {
                val yesterdayCal = Calendar.getInstance().apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                val yesterdayTimes = getPrayerTimesForDate(latitude, longitude, yesterdayCal.time, method)
                Triple(PrayerType.ISYA, "Isya", yesterdayTimes.isha)
            }
            currentStationLabel = "Sekarang: Menuju ${nextTarget.second}"
            nextStationLabel = "${nextTarget.second} (${timeFormat.format(nextTarget.third)})"
        }

        val prevStationLabel = "${prevStationTriple.second} (${timeFormat.format(prevStationTriple.third)})"
        val remainingMillis = (nextTarget.third.time - now.time).coerceAtLeast(0L)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60

        val countdownFormatted = if (hours > 0) {
            String.format(Locale.getDefault(), "Tersisa %02d jam %02d menit", hours, minutes)
        } else {
            String.format(Locale.getDefault(), "Tersisa %02d menit %02d detik", minutes, seconds)
        }

        val countdownShort = if (hours > 0) "${hours}j ${minutes}m lagi" else "${minutes}m lagi"
        val countdownDetailed = String.format(Locale.getDefault(), "Tersisa %02d:%02d:%02d", hours, minutes, seconds)

        val windowDuration = (nextTarget.third.time - prevStationTriple.third.time).toFloat().coerceAtLeast(1f)
        val elapsed = (now.time - prevStationTriple.third.time).toFloat().coerceAtLeast(0f)
        val progressFraction = (elapsed / windowDuration).coerceIn(0f, 1f)

        return NextPrayerInfo(
            type = nextTarget.first,
            name = nextTarget.second,
            time = nextTarget.third,
            timeFormatted = timeFormat.format(nextTarget.third),
            remainingMillis = remainingMillis,
            countdownFormatted = countdownFormatted,
            countdownShort = countdownShort,
            countdownDetailed = countdownDetailed,
            prevStationLabel = prevStationLabel,
            currentStationLabel = currentStationLabel,
            nextStationLabel = nextStationLabel,
            progressFraction = progressFraction
        )
    }

    /**
     * Membangun daftar linimasa jadwal shalat 6 waktu (Subuh, Terbit, Dzuhur, Ashar, Maghrib, Isya)
     * lengkap dengan status aktif, waktu yang telah lewat, atau waktu yang akan datang.
     */
    fun getPrayerTimelineList(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        now: Date = Date(),
        method: CalculationMethodType = CalculationMethodType.KEMENAG
    ): List<PrayerItemUiModel> {
        val prayerTimes = getPrayerTimesForDate(latitude, longitude, now, method)

        val subuh = prayerTimes.fajr
        val terbit = prayerTimes.sunrise
        val dzuhur = prayerTimes.dhuhr
        val ashar = prayerTimes.asr
        val maghrib = prayerTimes.maghrib
        val isya = prayerTimes.isha

        fun determineStatus(time: Date, nextTime: Date?): PrayerStatus {
            return when {
                now.before(time) -> PrayerStatus.UPCOMING
                nextTime != null && now.before(nextTime) -> PrayerStatus.ACTIVE
                nextTime == null && now.after(time) -> PrayerStatus.ACTIVE
                else -> PrayerStatus.PASSED
            }
        }

        fun determineSubtitle(name: String, status: PrayerStatus, targetTime: Date): String {
            return when (status) {
                PrayerStatus.PASSED -> if (name == "Terbit (Syuruq)") "Waktu Dhuha Dimulai" else "Selesai"
                PrayerStatus.ACTIVE -> "Pukul ${timeFormat.format(targetTime)} WIB"
                PrayerStatus.UPCOMING -> {
                    val diff = (targetTime.time - now.time).coerceAtLeast(0L)
                    val h = TimeUnit.MILLISECONDS.toHours(diff)
                    val m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                    if (h > 0) "Dalam ${h}j ${m}m" else "Dalam ${m}m"
                }
            }
        }

        val sSubuh = determineStatus(subuh, terbit)
        val sTerbit = determineStatus(terbit, dzuhur)
        val sDzuhur = determineStatus(dzuhur, ashar)
        val sAshar = determineStatus(ashar, maghrib)
        val sMaghrib = determineStatus(maghrib, isya)
        val sIsya = if (now.after(isya)) PrayerStatus.ACTIVE else PrayerStatus.UPCOMING

        return listOf(
            PrayerItemUiModel("subuh", PrayerType.SUBUH, "Subuh", determineSubtitle("Subuh", sSubuh, subuh), timeFormat.format(subuh), "WIB", sSubuh, sSubuh != PrayerStatus.PASSED),
            PrayerItemUiModel("terbit", PrayerType.TERBIT, "Terbit (Syuruq)", determineSubtitle("Terbit (Syuruq)", sTerbit, terbit), timeFormat.format(terbit), "WIB", sTerbit, false, false),
            PrayerItemUiModel("dzuhur", PrayerType.DZUHUR, "Dzuhur", determineSubtitle("Dzuhur", sDzuhur, dzuhur), timeFormat.format(dzuhur), "WIB", sDzuhur, sDzuhur != PrayerStatus.PASSED),
            PrayerItemUiModel("ashar", PrayerType.ASHAR, "Ashar", determineSubtitle("Ashar", sAshar, ashar), timeFormat.format(ashar), "WIB", sAshar, sAshar != PrayerStatus.PASSED),
            PrayerItemUiModel("maghrib", PrayerType.MAGHRIB, "Maghrib", determineSubtitle("Maghrib", sMaghrib, maghrib), timeFormat.format(maghrib), "WIB", sMaghrib, sMaghrib != PrayerStatus.PASSED),
            PrayerItemUiModel("isya", PrayerType.ISYA, "Isya", determineSubtitle("Isya", sIsya, isya), timeFormat.format(isya), "WIB", sIsya, true)
        )
    }

    /**
     * Menghasilkan keseluruhan snapshot [PrayerUiState] untuk layar dashboard utama
     * secara atomik dan konsisten pada satu titik waktu [now].
     */
    fun getFullPrayerUiState(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        locationName: String = DEFAULT_LOCATION_NAME,
        method: CalculationMethodType = CalculationMethodType.KEMENAG,
        now: Date = Date()
    ): PrayerUiState {
        val next = getNextPrayerInfo(latitude, longitude, now, method)
        val timeline = getPrayerTimelineList(latitude, longitude, now, method)
        val qibla = getQiblaInfo(latitude, longitude, locationName)

        val rakaatDesc = when (next.type) {
            PrayerType.SUBUH -> "Waktu Masuk Subuh • 2 Rakaat"
            PrayerType.DZUHUR -> "Waktu Masuk Dzuhur • 4 Rakaat"
            PrayerType.ASHAR -> "Waktu Masuk Ashar • 4 Rakaat"
            PrayerType.MAGHRIB -> "Waktu Masuk Maghrib • 3 Rakaat"
            PrayerType.ISYA -> "Waktu Masuk Isya • 4 Rakaat"
            PrayerType.TERBIT -> "Waktu Terbit Matahari"
        }

        return PrayerUiState(
            hijriDate = getFormattedHijriDate(),
            gregorianDate = formatIslamicGregorianDate(now),
            locationName = locationName,
            accuracyTag = "Akurat",
            heroPrayer = HeroPrayerUiModel(
                categoryLabel = "SHALAT BERIKUTNYA",
                prayerName = next.name.uppercase(),
                description = rakaatDesc,
                timeString = next.timeFormatted,
                timeZone = "WIB",
                countdownFormatted = next.countdownFormatted,
                prevPrayerInfo = next.prevStationLabel,
                currentStationInfo = next.currentStationLabel,
                nextPrayerInfo = next.nextStationLabel,
                progressFraction = next.progressFraction
            ),
            qiblaInfo = qibla,
            calculationMethod = CalculationMethodUiModel(
                title = "Metode Hisab",
                authority = method.title,
                parameters = method.parametersDescription
            ),
            prayerTimeline = timeline
        )
    }

    /**
     * Menghitung data state ringkas khusus untuk Glance AppWidget dan timestamp waktu shalat berikutnya
     * yang digunakan sebagai pemicu AlarmManager update widget.
     *
     * @return [Pair] berupa [PrayerWidgetState] untuk render UI widget dan [Long] timestamp shalat berikutnya.
     */
    fun getWidgetData(
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        locationShortName: String = "Jakarta",
        method: CalculationMethodType = CalculationMethodType.KEMENAG,
        now: Date = Date()
    ): Pair<PrayerWidgetState, Long> {
        val nextInfo = getNextPrayerInfo(latitude, longitude, now, method)
        val todayTimes = getPrayerTimesForDate(latitude, longitude, now, method)

        val tomorrowCal = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowTimes = getPrayerTimesForDate(latitude, longitude, tomorrowCal.time, method)

        val upcomingList = listOf(
            UpcomingPrayerWidgetItem("Maghrib", timeFormat.format(todayTimes.maghrib), com.prayertime.R.drawable.ic_widget_sunset),
            UpcomingPrayerWidgetItem("Isya", timeFormat.format(todayTimes.isha), com.prayertime.R.drawable.ic_widget_moon),
            UpcomingPrayerWidgetItem("Subuh (Bsk)", timeFormat.format(tomorrowTimes.fajr), com.prayertime.R.drawable.ic_widget_sunrise)
        )

        val widgetState = PrayerWidgetState(
            location = locationShortName,
            nextPrayerName = nextInfo.name.uppercase(),
            nextPrayerTime = nextInfo.timeFormatted,
            countdownShort = nextInfo.countdownShort,
            countdownDetailed = nextInfo.countdownDetailed,
            upcomingList = upcomingList
        )

        return Pair(widgetState, nextInfo.time.time)
    }

    private fun getFormattedHijriDate(): String {
        return try {
            val hijrahDate = HijrahDate.now()
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val year = hijrahDate.get(ChronoField.YEAR)
            val monthName = if (month in 1..12) HIJRI_MONTHS[month - 1] else "Syawal"
            "$day $monthName $year H"
        } catch (e: Exception) {
            "14 Syawal 1445 H"
        }
    }

    private fun getHijriDayMonthForDate(date: Date): String {
        return try {
            val localDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            val hijrahDate = HijrahDate.from(localDate)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val monthName = if (month in 1..12) HIJRI_MONTHS[month - 1] else "Syawal"
            "$day $monthName"
        } catch (e: Exception) {
            "14 Syawal"
        }
    }

    private fun getCardinalDirection(deg: Double): String {
        val normalized = (deg % 360 + 360) % 360
        return when {
            normalized in 22.5..67.5 -> "Timur Laut"
            normalized in 67.5..112.5 -> "Timur"
            normalized in 112.5..157.5 -> "Tenggara"
            normalized in 157.5..202.5 -> "Selatan"
            normalized in 202.5..247.5 -> "Barat Daya"
            normalized in 247.5..292.5 -> "Barat"
            normalized in 292.5..337.5 -> "Barat Laut"
            else -> "Utara"
        }
    }
}
