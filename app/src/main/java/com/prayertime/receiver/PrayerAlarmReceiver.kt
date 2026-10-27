package com.prayertime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.prayertime.notification.PrayerAlarmScheduler
import com.prayertime.notification.PrayerNotificationHelper
import com.prayertime.widget.PrayerWidgetScheduler

/**
 * [BroadcastReceiver] penerima pemicu alarm shalat dari [android.app.AlarmManager] serta sinyal sistem [Intent.ACTION_BOOT_COMPLETED].
 * Bertanggung jawab menampilkan notifikasi waktu shalat, memperbarui widget layar depan,
 * dan menjadwalkan ulang seluruh alarm shalat berikutnya.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        /** Aksi siaran saat waktu shalat tiba */
        const val ACTION_PRAYER_ALARM = "com.prayertime.ACTION_PRAYER_ALARM"
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_CITY_NAME = "extra_city_name"
        private const val TAG = "PrayerAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive dipicu dengan action: $action")

        when (action) {
            ACTION_PRAYER_ALARM -> {
                val prayerId = intent.getStringExtra(EXTRA_PRAYER_ID) ?: "shalat"
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Shalat"
                val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
                val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "Indonesia"

                Log.d(TAG, "Menampilkan notifikasi shalat $prayerName ($prayerTime) untuk $cityName")

                // 1. Tampilkan notifikasi suara & getar
                PrayerNotificationHelper.showPrayerNotification(
                    context = context,
                    prayerId = prayerId,
                    prayerName = prayerName,
                    prayerTime = prayerTime,
                    cityName = cityName
                )

                // 2. Perbarui seluruh widget layar depan
                PrayerWidgetScheduler.updateAllWidgets(context)

                // 3. Jadwalkan ulang alarm shalat berikutnya (untuk besok / waktu selanjutnya)
                PrayerAlarmScheduler.rescheduleAllAlarms(context)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device rebooted, menjadwalkan ulang seluruh alarm shalat & widget...")
                PrayerAlarmScheduler.rescheduleAllAlarms(context)
                PrayerWidgetScheduler.scheduleNextPrayerFromNow(context)
            }
        }
    }
}
