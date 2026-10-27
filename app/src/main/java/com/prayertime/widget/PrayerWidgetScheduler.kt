package com.prayertime.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.prayertime.data.datastore.PrayerPreferences
import com.prayertime.data.repository.CalculationMethodType
import com.prayertime.data.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Penjadwal dan orkestrator pembaruan berkala Jetpack Glance AppWidget.
 * Menggunakan strategi hemat baterai dengan [android.app.AlarmManager]:
 * - Menjadwalkan pemicu tepat pada jam pergantian shalat berikutnya (`setExactAndAllowWhileIdle`).
 * - Menyegarkan hitung mundur jam/menit dengan interval adaptif 30 menit tanpa loop per-detik yang boros daya.
 * - Memperbarui seluruh instance widget seketika saat pengaturan lokasi atau metode hisab diubah dari aplikasi.
 */
object PrayerWidgetScheduler {

    private const val TAG = "PrayerWidgetScheduler"
    private const val REQUEST_CODE_PRAYER_ALARM = 1001

    /**
     * Daftarkan AlarmManager persis pada waktu shalat berikutnya masuk
     */
    fun scheduleNextPrayerAlarm(context: Context, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Trigger time sudah lewat, mengabaikan pendaftaran alarm.")
            return
        }

        val intent = Intent(context, PrayerGlanceWidgetReceiver::class.java).apply {
            action = PrayerGlanceWidgetReceiver.ACTION_PRAYER_TIME_TRIGGER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PRAYER_ALARM,
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
                    Log.d(TAG, "Exact alarm didaftarkan via setExactAndAllowWhileIdle: ${Date(triggerAtMillis)}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Inexact idle alarm didaftarkan: ${Date(triggerAtMillis)}")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm didaftarkan: ${Date(triggerAtMillis)}")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException saat mendaftarkan exact alarm: ${e.message}")
        }
    }

    /**
     * Hitung jadwal shalat terdekat berikutnya menggunakan lokasi tersimpan dan jadwalkan alarm.
     * Menggunakan strategi adaptif: pasang alarm tepat pada jam masuk shalat, atau interval 30 menit
     * jika shalat berikutnya masih jauh, agar sisa jam/menit di widget tetap akurat tanpa menguras baterai.
     */
    fun scheduleNextPrayerFromNow(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferences = PrayerPreferences(context)
                val savedLocation = preferences.getSavedLocationDirect()
                val savedMethodId = preferences.getSavedMethodDirect()
                val method = CalculationMethodType.fromId(savedMethodId)

                val repository = PrayerRepository()
                val nextInfo = repository.getNextPrayerInfo(
                    latitude = savedLocation.latitude,
                    longitude = savedLocation.longitude,
                    method = method
                )

                val nextPrayerTimeMillis = nextInfo.time.time
                val nowMillis = System.currentTimeMillis()
                val thirtyMinutesMillis = nowMillis + 30 * 60 * 1000L

                // Pasang trigger pada waktu shalat masuk atau maksimal 30 menit ke depan
                val targetTriggerMillis = if (nextPrayerTimeMillis in (nowMillis + 1)..thirtyMinutesMillis) {
                    nextPrayerTimeMillis
                } else {
                    thirtyMinutesMillis
                }

                scheduleNextPrayerAlarm(context, targetTriggerMillis)
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduleNextPrayerFromNow: ${e.message}")
            }
        }
    }

    /**
     * Update seluruh instance Glance Widget seketika saat lokasi / metode berubah
     */
    fun updateAllWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val glanceManager = GlanceAppWidgetManager(context)
                val glanceIds = glanceManager.getGlanceIds(PrayerGlanceWidget::class.java)
                for (glanceId in glanceIds) {
                    PrayerGlanceWidget().update(context, glanceId)
                    Log.d(TAG, "Widget $glanceId updated successfully from app event.")
                }
                scheduleNextPrayerFromNow(context)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal meng-update widget dari event aplikasi: ${e.message}")
            }
        }
    }

    /**
     * Batalkan alarm terjadwal
     */
    fun cancelScheduledAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PrayerGlanceWidgetReceiver::class.java).apply {
            action = PrayerGlanceWidgetReceiver.ACTION_PRAYER_TIME_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PRAYER_ALARM,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
