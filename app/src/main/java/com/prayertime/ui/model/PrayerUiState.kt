package com.prayertime.ui.model

// ============================================================================
// State Models & Enums for Prayer App Screen
// ============================================================================

/**
 * Status fase waktu shalat harian.
 */
enum class PrayerStatus {
    /** Waktu shalat telah berlalu (ditampilkan dengan ikon centang dan warna redup) */
    PASSED,
    /** Jendela waktu shalat yang sedang aktif berlangsung saat ini (kartu disorot dengan indikator aktif) */
    ACTIVE,
    /** Waktu shalat berikutnya yang akan datang (ditampilkan dengan kontras jelas dan hitung mundur) */
    UPCOMING
}

/**
 * Jenis waktu shalat dan penanda matahari harian.
 */
enum class PrayerType {
    SUBUH,
    TERBIT,
    DZUHUR,
    ASHAR,
    MAGHRIB,
    ISYA
}

/**
 * Item menu navigasi utama pada Bottom Navigation Bar.
 */
enum class NavigationItem(val label: String) {
    SHALAT("Shalat"),
    KIBLAT("Kiblat"),
    DOA("Doa"),
    PENGATURAN("Pengaturan")
}

/**
 * Model data UI untuk merepresentasikan satu baris waktu shalat pada daftar timeline harian.
 *
 * @property id Identifier unik shalat (misal: "subuh", "dzuhur").
 * @property type Tipe shalat ([PrayerType]).
 * @property name Nama tampilan shalat (misal: "Subuh", "Terbit (Syuruq)").
 * @property subtitle Deskripsi pelengkap (misal: "Selesai", "Dalam 2j 28m").
 * @property timeString Waktu masuk shalat dalam format "HH:mm".
 * @property timeZone Zona waktu lokal (misal: "WIB", "WITA", "WIT").
 * @property status Status aktif/berlalu ([PrayerStatus]).
 * @property isNotificationEnabled Apakah pengingat notifikasi aktif.
 * @property isFardhu Apakah shalat fardhu (true) atau penanda terbit (false).
 */
data class PrayerItemUiModel(
    val id: String,
    val type: PrayerType,
    val name: String,
    val subtitle: String,
    val timeString: String,
    val timeZone: String = "WIB",
    val status: PrayerStatus,
    val isNotificationEnabled: Boolean = true,
    val isFardhu: Boolean = true
)

/**
 * Model data UI untuk kartu Hero Shalat Berikutnya di bagian atas dashboard.
 *
 * @property categoryLabel Label kategori kartu (misal: "SHALAT BERIKUTNYA").
 * @property prayerName Nama shalat berikutnya (misal: "ASHAR").
 * @property description Keterangan jumlah rakaat atau status awal.
 * @property timeString Jam masuk shalat (misal: "15:24").
 * @property timeZone Zona waktu lokal.
 * @property countdownFormatted Teks hitung mundur (misal: "Tersisa 01 jam 45 menit").
 * @property prevPrayerInfo Informasi shalat sebelumnya pada bar progres.
 * @property currentStationInfo Teks stasiun waktu saat ini.
 * @property nextPrayerInfo Informasi shalat berikutnya pada bar progres.
 * @property progressFraction Fraksi durasi waktu yang telah berjalan antara 0.0f sampai 1.0f.
 */
data class HeroPrayerUiModel(
    val categoryLabel: String = "SHALAT BERIKUTNYA",
    val prayerName: String,
    val description: String,
    val timeString: String,
    val timeZone: String = "WIB",
    val countdownFormatted: String,
    val prevPrayerInfo: String,
    val currentStationInfo: String,
    val nextPrayerInfo: String,
    val progressFraction: Float // 0f to 1f
)

/**
 * Model data UI untuk kartu ringkasan arah kiblat.
 */
data class QiblaInfoUiModel(
    val title: String = "Arah Kiblat",
    val degrees: String = "295°",
    val direction: String = "Barat Laut",
    val subtitle: String = "Dari posisi Jakarta"
)

/**
 * Model data UI untuk kartu informasi metode hisab yang sedang diterapkan.
 */
data class CalculationMethodUiModel(
    val title: String = "Metode Hisab",
    val authority: String = "Kemenag RI",
    val parameters: String = "Subuh 20°, Isya 18° (+2-5m Ihtiyat)"
)

/**
 * Model data UI untuk kartu akses cepat tasbih digital.
 */
data class TasbihQuickUiModel(
    val title: String = "Tasbih Digital",
    val targetDescription: String = "Target Subhanallah: 33x",
    val buttonLabel: String = "Mulai"
)

/**
 * Representasi StateFlow keseluruhan tampilan layar utama jadwal shalat ([PrayerUiState]).
 */
data class PrayerUiState(
    val hijriDate: String = "14 Syawal 1445 H",
    val gregorianDate: String = "Senin, 22 April 2024",
    val locationName: String = "Jakarta, Indonesia",
    val accuracyTag: String = "Akurat",
    val heroPrayer: HeroPrayerUiModel,
    val qiblaInfo: QiblaInfoUiModel = QiblaInfoUiModel(),
    val calculationMethod: CalculationMethodUiModel = CalculationMethodUiModel(),
    val prayerTimeline: List<PrayerItemUiModel>,
    val tasbihQuickAction: TasbihQuickUiModel = TasbihQuickUiModel(),
    val currentSelectedTab: NavigationItem = NavigationItem.SHALAT
)

// ============================================================================
// Factory Data Pratinjau (Preview / Mock)
// ============================================================================

/**
 * Menghasilkan objek [PrayerUiState] tiruan untuk kebutuhan Composable Preview dan testing UI.
 */
fun createDummyPrayerUiState(): PrayerUiState {
    return PrayerUiState(
        hijriDate = "14 Syawal 1445 H",
        gregorianDate = "Senin, 22 April 2024",
        locationName = "Jakarta, Indonesia",
        accuracyTag = "Akurat",
        heroPrayer = HeroPrayerUiModel(
            categoryLabel = "SHALAT BERIKUTNYA",
            prayerName = "ASHAR",
            description = "Waktu Masuk Ashar Awal • 4 Rakaat",
            timeString = "15:24",
            timeZone = "WIB",
            countdownFormatted = "Tersisa 01 jam 45 menit",
            prevPrayerInfo = "Dzuhur (11:55)",
            currentStationInfo = "Sekarang: Menuju Ashar",
            nextPrayerInfo = "Maghrib (17:52)",
            progressFraction = 0.72f
        ),
        qiblaInfo = QiblaInfoUiModel(
            title = "Arah Kiblat",
            degrees = "295°",
            direction = "Barat Laut",
            subtitle = "Dari posisi Jakarta"
        ),
        calculationMethod = CalculationMethodUiModel(
            title = "Metode Hisab",
            authority = "Kemenag RI",
            parameters = "Subuh 20°, Isya 18° (+2-5m Ihtiyat)"
        ),
        prayerTimeline = listOf(
            PrayerItemUiModel(
                id = "1",
                type = PrayerType.SUBUH,
                name = "Subuh",
                subtitle = "Selesai",
                timeString = "04:38",
                timeZone = "WIB",
                status = PrayerStatus.PASSED,
                isNotificationEnabled = false,
                isFardhu = true
            ),
            PrayerItemUiModel(
                id = "2",
                type = PrayerType.TERBIT,
                name = "Terbit (Syuruq)",
                subtitle = "Waktu Dhuha Dimulai",
                timeString = "05:54",
                timeZone = "WIB",
                status = PrayerStatus.PASSED,
                isNotificationEnabled = false,
                isFardhu = false
            ),
            PrayerItemUiModel(
                id = "3",
                type = PrayerType.DZUHUR,
                name = "Dzuhur",
                subtitle = "Selesai",
                timeString = "11:55",
                timeZone = "WIB",
                status = PrayerStatus.PASSED,
                isNotificationEnabled = false,
                isFardhu = true
            ),
            PrayerItemUiModel(
                id = "4",
                type = PrayerType.ASHAR,
                name = "Ashar",
                subtitle = "Pukul 15:24 WIB",
                timeString = "15:24",
                timeZone = "WIB",
                status = PrayerStatus.ACTIVE,
                isNotificationEnabled = true,
                isFardhu = true
            ),
            PrayerItemUiModel(
                id = "5",
                type = PrayerType.MAGHRIB,
                name = "Maghrib",
                subtitle = "Dalam 2j 28m",
                timeString = "17:52",
                timeZone = "WIB",
                status = PrayerStatus.UPCOMING,
                isNotificationEnabled = true,
                isFardhu = true
            ),
            PrayerItemUiModel(
                id = "6",
                type = PrayerType.ISYA,
                name = "Isya",
                subtitle = "Malam ini",
                timeString = "19:03",
                timeZone = "WIB",
                status = PrayerStatus.UPCOMING,
                isNotificationEnabled = true,
                isFardhu = true
            )
        ),
        tasbihQuickAction = TasbihQuickUiModel(
            title = "Tasbih Digital",
            targetDescription = "Target Subhanallah: 33x",
            buttonLabel = "Mulai"
        ),
        currentSelectedTab = NavigationItem.SHALAT
    )
}
