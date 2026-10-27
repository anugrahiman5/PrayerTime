package com.prayertime.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * [GlanceAppWidgetReceiver] penerima aksi pemicu waktu shalat dan penyalaan ulang perangkat ([Intent.ACTION_BOOT_COMPLETED]).
 * Menjalankan pembaruan asinkron pada seluruh widget yang terpasang di launcher
 * dan memperpanjang siklus alarm ke shalat berikutnya secara otonom.
 */
class PrayerGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        /** Aksi siaran pemicu pembaruan berkala widget */
        const val ACTION_PRAYER_TIME_TRIGGER = "com.prayertime.ACTION_PRAYER_TIME_TRIGGER"
        private const val TAG = "PrayerWidgetReceiver"
    }

    override val glanceAppWidget: GlanceAppWidget = PrayerGlanceWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d(TAG, "onReceive intent: ${intent.action}")

        when (intent.action) {
            ACTION_PRAYER_TIME_TRIGGER, Intent.ACTION_BOOT_COMPLETED -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val glanceManager = GlanceAppWidgetManager(context)
                        val glanceIds = glanceManager.getGlanceIds(PrayerGlanceWidget::class.java)

                        // Perbarui seluruh instance Glance Widget di launcher
                        for (glanceId in glanceIds) {
                            PrayerGlanceWidget().update(context, glanceId)
                            Log.d(TAG, "Widget $glanceId berhasil di-update ke jadwal shalat berikutnya.")
                        }

                        // Jadwalkan alarm berikutnya secara otomatis (siklus otonom hemat daya)
                        PrayerWidgetScheduler.scheduleNextPrayerFromNow(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Gagal meng-update widget: ${e.message}", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
