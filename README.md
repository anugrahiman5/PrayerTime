# 🕌 Prayer Time (Jadwal Shalat & Kiblat) - Native Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7C4DFF.svg?style=flat)](https://m3.material.io)
[![Jetpack Glance](https://img.shields.io/badge/Widget-Jetpack%20Glance-0F9D58.svg?style=flat)](https://developer.android.com/jetpack/androidx/releases/glance)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-orange.svg?style=flat)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-34%20(Android%2014)-brightgreen.svg?style=flat)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Aplikasi jadwal waktu shalat modern, akurat, dan ringan untuk sistem operasi Android. Dibangun 100% secara native menggunakan **Jetpack Compose**, **Material 3**, serta **Jetpack Glance AppWidget**, dengan standar hisab resmi **Kementerian Agama Republik Indonesia (Kemenag RI)** lengkap dengan waktu pengaman (*Ihtiyat* +2 menit).

---

## ✨ Fitur Utama (Key Features)

- ⏱️ **Jadwal Shalat Akurat & Sinkron (Kemenag RI)**
  - Menghitung waktu Subuh, Syuruq (Terbit), Dzuhur, Ashar, Maghrib, dan Isya secara real-time berdasarkan posisi lintang & bujur.
  - Mengadopsi standar sudut Subuh 20° dan Isya 18° serta **Ihtiyat (+2-5 menit)** resmi Kemenag RI sehingga waktu shalat selalu akurat dan sinkron dengan jam digital masjid di Indonesia.
  - Pilihan otoritas hisab global lainnya: *Muslim World League (MWL)*, *Umm Al-Qura (Makkah)*, *Egyptian General Authority*, dan *MUIS Singapura*.

- 📱 **Glance AppWidget Interaktif (Home Screen)**
  - Menggunakan **Jetpack Glance** modern dengan dukungan adaptif otomatis *Light Mode* & *Dark Mode*.
  - Dua varian tata letak responsif:
    - **Compact Layout (4x1 / 2x2)**: Nama kota, jam shalat berikutnya, dan lencana hitung mundur.
    - **Expanded Layout (4x2)**: Kartu hero utama di sisi kiri dan daftar 3 waktu shalat mendatang di sisi kanan.
    - Siklus pembaruan otomatis hemat daya via `AlarmManager` saat waktu shalat tiba (tanpa loop per-detik yang menguras baterai).

- 🧭 **Kompas Arah Kiblat Real-Time**
  - Menggunakan sensor `TYPE_ROTATION_VECTOR` perangkat untuk rotasi jarum kompas yang halus (*60fps*).
  - Perhitungan sudut azimut kiblat geodesik (*Great Circle Bearing*) dan jarak lengkung bumi (*Haversine*) menuju Ka'bah di Makkah (Lat: 21.4225, Lon: 39.8262).
  - Dilengkapi indikator visual presisi serta **getaran haptik** (*Haptic Feedback*) ketika perangkat tepat menghadap arah kiblat (toleransi ±3.5°).

- 🗺️ **Database Offline 514 Kabupaten/Kota Se-Indonesia**
  - Memuat dataset lokal lengkap seluruh 514 Kabupaten dan Kota di 38 Provinsi Indonesia beserta koordinat presisi.
  - Pencarian luring instan (*offline search*) tanpa koneksi internet dengan kecepatan `<1ms`.
  - Dilengkapi fitur deteksi koordinat otomatis berbasis GPS (*Fused Location Provider*) serta fallback pencarian daring via *Android Geocoder* untuk pencarian hingga level kecamatan/kelurahan.

- 📅 **Jadwal Shalat 7 Hari (Pekanan) & Penamaan Hari Islam**
  - Modal bottom sheet jadwal sepekan ke depan dengan penanggalan Hijriah dan Masehi.
  - Standarisasi penamaan hari Islam: mengganti sebutan *Minggu* menjadi **Ahad** (*Ahad, Senin, Selasa, Rabu, Kamis, Jumat, Sabtu*).

- 📿 **Tasbih Digital Interaktif**
  - Counter dzikir digital dengan animasi tekan membal (*spring bounce effect*).
  - Umpan balik getaran ringan (*Haptic Feedback*) pada setiap ketukan.
  - Pilihan lafadz dzikir (*Subhanallah, Alhamdulillah, Allahu Akbar, Astaghfirullah, Laa ilaaha illallah*), target default 33x, indikator visual pencapaian target, dan tombol reset.

- 🔔 **Pengingat Notifikasi & Alarm Tepat Waktu**
  - Notifikasi suara dan getar tepat saat waktu shalat masuk menggunakan `AlarmManager.setExactAndAllowWhileIdle`.
  - Toggle lonceng per waktu shalat (bisa diaktifkan/dinonaktifkan per jadwal).
  - Mendukung penuh sistem perizinan runtime notifikasi Android 13+ (*POST_NOTIFICATIONS*).
  - Penjadwalan ulang otomatis saat perangkat selesai melakukan boot ulang (*RECEIVE_BOOT_COMPLETED*).

- 📖 **Kumpulan Doa Harian & Dzikir**
  - Dilengkapi teks Arab berharakat, transliterasi latin, dan arti terjemahan dalam Bahasa Indonesia.

---

## 🛠️ Tech Stack & Arsitektur

- **Bahasa**: [Kotlin](https://kotlinlang.org/) (100%)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI)
- **Widget**: [Jetpack Glance AppWidget](https://developer.android.com/jetpack/androidx/releases/glance)
- **Design System**: [Material Design 3](https://m3.material.io/) (Light & Dark theme)
- **Navigasi**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation) (Single Activity with Unified Backstack)
- **Arsitektur**: Model-View-ViewModel (MVVM) + Unidirectional Data Flow (UDF)
- **Asinkron & Reaktif**: Kotlin Coroutines & `StateFlow`
- **Penyimpanan Lokal**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Hisab Astronomis**: [Batoulapps Adhan Java/Android](https://github.com/batoulapps/adhan-java)
- **Lokasi & GPS**: Google Play Services Location (`FusedLocationProviderClient`) + Android Geocoder

---

## 📂 Struktur Direktori Proyek

```text
com.prayertime/
├── MainActivity.kt                      # Titik masuk utama & konfigurasi root navigation
├── data/
│   ├── datastore/
│   │   └── PrayerPreferences.kt         # Penyimpanan lokal DataStore Preferences
│   ├── location/
│   │   ├── CityPreset.kt                # Model data daerah
│   │   ├── IndonesiaDistricts.kt        # Dataset luring 514 Kab/Kota se-Indonesia
│   │   └── LocationManager.kt           # Manajemen GPS, Geocoder, dan filter lokasi
│   └── repository/
│       └── PrayerRepository.kt          # Kalkulasi waktu shalat, Ihtiyat +2m, dan arah kiblat
├── notification/
│   ├── PrayerAlarmScheduler.kt          # Penjadwalan AlarmManager tepat waktu
│   └── PrayerNotificationHelper.kt      # Pembuatan NotificationChannel & notifikasi status bar
├── receiver/
│   └── PrayerAlarmReceiver.kt           # BroadcastReceiver pemicu waktu shalat & boot
├── ui/
│   ├── components/                      # Komponen UI modular Jetpack Compose
│   │   ├── BottomQuickActions.kt        # Bottom Navigation Bar
│   │   ├── CalculationMethodBottomSheet.kt
│   │   ├── HeroPrayerCard.kt            # Kartu utama waktu shalat & progress bar
│   │   ├── LocationSelectionBottomSheet.kt
│   │   ├── PrayerTimelineList.kt        # Linimasa jadwal 6 waktu & toggle lonceng
│   │   ├── QuickInfoCards.kt            # Kartu ringkas Kiblat & Metode Hisab
│   │   ├── TasbihModalSheet.kt          # Counter tasbih digital interaktif
│   │   ├── TopLocationBar.kt            # Bar tanggal Hijriah & lokasi
│   │   └── WeeklyScheduleBottomSheet.kt # Jadwal shalat 7 hari (Pekanan)
│   ├── model/
│   │   └── PrayerUiState.kt             # Data class state UI dashboard
│   ├── screens/
│   │   ├── DoaScreen.kt                 # Layar kumpulan doa harian
│   │   ├── PrayerMainScreen.kt          # Layar utama dashboard jadwal shalat
│   │   ├── QiblaCompassScreen.kt        # Layar kompas kiblat interaktif
│   │   └── SettingsScreen.kt            # Layar pengaturan preferensi
│   ├── theme/                           # Color, Typography, Shape & Theme Material 3
│   └── viewmodel/
│       └── PrayerViewModel.kt           # ViewModel pengendali state & coroutines
└── widget/
    ├── PrayerGlanceWidget.kt            # Implementasi Jetpack Glance AppWidget
    ├── PrayerGlanceWidgetReceiver.kt    # Glance AppWidget Receiver
    └── PrayerWidgetScheduler.kt         # Penjadwal pembaruan widget via AlarmManager
```

---

## 🚀 Cara Menjalankan & Membangun (Getting Started)

### Prasyarat
1. **Android Studio** Hedgehog (2023.1.1) atau versi yang lebih baru (Ladybug / Koala).
2. **JDK 17** (default bawaan Android Studio).
3. Perangkat Android fisik atau Emulator dengan minimal **Android 8.0 (API Level 26)**.

### Langkah Instalasi
1. Clone repositori ini:
   ```bash
   git clone https://github.com/anugrahiman5/PrayerTime.git
   cd PrayerTime
   ```
2. Buka proyek di **Android Studio**:
   - Pilih `File` > `Open...` dan arahkan ke direktori `PrayerTime`.
3. Tunggu hingga proses **Gradle Sync** selesai.
4. Hubungkan perangkat Android fisik atau jalankan Android Emulator.
5. Tekan tombol **Run** (`Shift + F10`) atau jalankan perintah Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📄 Lisensi (License)

Proyek ini dilisensikan di bawah lisensi **MIT License** - silakan lihat berkas [LICENSE](LICENSE) untuk detail lengkap.
