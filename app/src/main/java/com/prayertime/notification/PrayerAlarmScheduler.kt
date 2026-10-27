package com.prayertime.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.prayertime.data.datastore.PrayerPreferences
import com.prayertime.data.repository.CalculationMethodType
import com.prayertime.data.repository.PrayerRepository
import com.prayertime.receiver.PrayerAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Penjadwal alarm pengingat waktu shalat menggunakan sistem [AlarmManager] Android.
 * Mengelola pendaftaran alarm tepat waktu (`setExactAndAllowWhileIdle`),
 * pembatalan alarm per shalat, dan penjadwalan ulang berkala saat perubahan lokasi/waktu.
 */
object PrayerAlarmScheduler {

    private const val TAG = "PrayerAlarmScheduler"
    val ACTION_PRAYER_ALARM = PrayerAlarmReceiver.ACTION_PRAYER_ALARM

    /**
     * Menghasilkan request code unik [PendingIntent] berdasarkan ID waktu shalat
     * untuk mencegah bentrokan intent antar jadwal shalat.
     */
    fun getPrayerRequestCode(prayerId: String): Int {
        return when (prayerId.lowercase()) {
            "subuh" -> 201
            "dzuhur" -> 202
            "ashar" -> 203
            "maghrib" -> 204
            "isya" -> 205
            else -> 200
        }
    }

    /**
     * Mendaftarkan alarm tepat pada jam masuk shalat tertentu ke sistem [AlarmManager].
     * Menggunakan strategi hemat baterai dengan `setExactAndAllowWhileIdle` agar alarm tetap bunyi saat Doze mode.
     *
     * @param context Konteks aplikasi.
     * @param prayerId Identifier shalat ("subuh", "dzuhur", dll).
     * @param prayerName Nama shalat tampilan.
     * @param prayerTime Waktu shalat terformat (HH:mm).
     * @param cityName Nama kota/daerah untuk isi notifikasi.
     * @param triggerAtMillis Waktu pemicu alarm dalam milidetik (Epoch time).
     */
    fun scheduleSingleAlarm(
        context: Context,
        prayerId: String,
        prayerName: String,
        prayerTime: String,
        cityName: String,
        triggerAtMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Trigger time untuk $prayerId sudah lewat, mengabaikan.")
            return
        }

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, prayerId)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, prayerTime)
            putExtra(PrayerAlarmReceiver.EXTRA_CITY_NAME, cityName)
        }

        val requestCode = getPrayerRequestCode(prayerId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Alarm $prayerName didaftarkan: ${Date(triggerAtMillis)}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Inexact idle alarm $prayerName didaftarkan: ${Date(triggerAtMillis)}")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm $prayerName didaftarkan: ${Date(triggerAtMillis)}")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException saat menjadwalkan alarm $prayerId: ${e.message}")
        }
    }

    /**
     * Batalkan alarm shalat tertentu saat lonceng dinonaktifkan
     */
    fun cancelSingleAlarm(context: Context, prayerId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
        }
        val requestCode = getPrayerRequestCode(prayerId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Alarm $prayerId berhasil dibatalkan.")
        }
    }

    /**
     * Hitung waktu dan jadwalkan ulang seluruh alarm shalat yang aktif
     */
    fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferences = PrayerPreferences(context)
                val location = preferences.getSavedLocationDirect()
                val methodId = preferences.getSavedMethodDirect()
                val method = CalculationMethodType.fromId(methodId)
                val notifSettings = preferences.getNotificationSettingsDirect()

                val repository = PrayerRepository()
                val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
                val cityName = location.cityName.split(",").firstOrNull()?.trim() ?: "Indonesia"

                val now = Calendar.getInstance()

                // Jadwalkan untuk masing-masing 5 waktu shalat fardhu
                val prayerList = listOf(
                    Triple("subuh", "Subuh") { times: com.batoulapps.adhan.PrayerTimes -> times.fajr },
                    Triple("dzuhur", "Dzuhur") { times: com.batoulapps.adhan.PrayerTimes -> times.dhuhr },
                    Triple("ashar", "Ashar") { times: com.batoulapps.adhan.PrayerTimes -> times.asr },
                    Triple("maghrib", "Maghrib") { times: com.batoulapps.adhan.PrayerTimes -> times.maghrib },
                    Triple("isya", "Isya") { times: com.batoulapps.adhan.PrayerTimes -> times.isha }
                )

                for (prayer in prayerList) {
                    val prayerId = prayer.first
                    val prayerName = prayer.second
                    val isEnabled = notifSettings[prayerId] ?: true

                    if (!isEnabled) {
                        cancelSingleAlarm(context, prayerId)
                        continue
                    }

                    // Ambil waktu hari ini
                    val todayTimes = repository.getPrayerTimesForDate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        date = now.time,
                        method = method
                    )
                    val prayerDateToday = prayer.third(todayTimes)

                    val targetDate: Date
                    if (prayerDateToday.after(now.time)) {
                        targetDate = prayerDateToday
                    } else {
                        // Jika jam shalat hari ini sudah lewat, jadwalkan untuk besok
                        val tomorrow = Calendar.getInstance().apply {
                            time = now.time
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        val tomorrowTimes = repository.getPrayerTimesForDate(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            date = tomorrow.time,
                            method = method
                        )
                        targetDate = prayer.third(tomorrowTimes)
                    }

                    scheduleSingleAlarm(
                        context = context,
                        prayerId = prayerId,
                        prayerName = prayerName,
                        prayerTime = timeFormat.format(targetDate),
                        cityName = cityName,
                        triggerAtMillis = targetDate.time
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal rescheduleAllAlarms: ${e.message}")
            }
        }
    }
}
