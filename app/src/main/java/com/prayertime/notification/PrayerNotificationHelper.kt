package com.prayertime.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prayertime.MainActivity
import com.prayertime.R

/**
 * Helper utilitas untuk membangun dan menampilkan notifikasi pengingat waktu shalat ke status bar Android.
 * Mengelola pembuatan [NotificationChannel] prioritas tinggi, suara notifikasi, dan pola getaran.
 */
object PrayerNotificationHelper {

    /** Identifier saluran notifikasi pengingat waktu shalat */
    const val CHANNEL_ID = "prayer_time_reminder_channel"
    /** Nama tampilan saluran di pengaturan sistem Android */
    const val CHANNEL_NAME = "Pengingat Waktu Shalat"
    /** Deskripsi fungsi saluran notifikasi */
    const val CHANNEL_DESC = "Notifikasi suara dan getar saat jam shalat masuk"

    /**
     * Membuat [NotificationChannel] pada Android 8.0+ (API 26+) dengan tingkat kepentingan [NotificationManager.IMPORTANCE_HIGH].
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setSound(soundUri, audioAttributes)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Membangun dan menampilkan notifikasi visual, suara, dan getar saat jam shalat masuk.
     * Menguji izin runtime `POST_NOTIFICATIONS` untuk Android 13+ (API 33+) secara aman.
     *
     * @param context Konteks aplikasi.
     * @param prayerId Identifier shalat (misal: "subuh", "ashar").
     * @param prayerName Nama shalat tampilan (misal: "Ashar").
     * @param prayerTime Jam masuk shalat (misal: "15:24").
     * @param cityName Nama kota/daerah untuk teks notifikasi.
     */
    fun showPrayerNotification(
        context: Context,
        prayerId: String,
        prayerName: String,
        prayerTime: String,
        cityName: String
    ) {
        // Buat channel jika belum dibuat
        createNotificationChannel(context)

        // Verifikasi izin notifikasi di Android 13+ (Tiramisu / API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_widget_bell)
            .setContentTitle("Waktu Shalat $prayerName Telah Tiba")
            .setContentText("Pukul $prayerTime WIB untuk wilayah $cityName")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Waktu masuk shalat $prayerName ($prayerTime WIB) untuk wilayah $cityName. Mari tunaikan shalat tepat waktu."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(prayerId.hashCode(), notification)
    }
}
