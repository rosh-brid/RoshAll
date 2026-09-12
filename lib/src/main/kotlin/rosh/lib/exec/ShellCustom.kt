package rosh.lib.exec

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import rosh.lib.exec.TutupService

class ShellCustom : Service() {

    companion object {
        private const val CHANNEL_ID = "shell_channel"
        private const val NOTIF_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        buatChannel()                                        // 1️⃣ channel dulu
        startForeground(NOTIF_ID, buatNotifikasi())          // 2️⃣ baru notifikasi
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Notifikasi foreground otomatis hilang saat service berhenti
    }

    private fun buatChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Terminal",
            NotificationManager.IMPORTANCE_LOW   // tanpa suara
        ).apply {
            description = "Notifikasi shell terminal"
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buatNotifikasi(): Notification {
        val intentTutup = Intent(this, TutupService::class.java).apply {
            action = TutupService.AKSI_TUTUP
        }
        val pendingTutup = PendingIntent.getBroadcast(
            this, 0, intentTutup,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Terminal is running")
            .setContentText("Shell aktif")
            .setSmallIcon(rosh.lib.R.drawable.terminal)
            .setOngoing(true)
            .addAction(0, "Tutup", pendingTutup)
            .build()
    }
}
