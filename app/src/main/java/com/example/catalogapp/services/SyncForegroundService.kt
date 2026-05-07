package com.example.catalogapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.catalogapp.MainActivity
import kotlinx.coroutines.*

class SyncForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "sync_foreground_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentProgress = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ✅ CORREGIDO: 4 parámetros (progress, operation, productId, productTitle)
        startForeground(NOTIFICATION_ID, createNotification(0, "sync", 0, ""))

        val operation = intent?.getStringExtra("operation") ?: "sync"
        val productId = intent?.getIntExtra("product_id", 0) ?: 0
        val productTitle = intent?.getStringExtra("product_title") ?: ""

        serviceScope.launch {
            syncData(operation, productId, productTitle)
            stopForeground(true)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private suspend fun syncData(operation: String, productId: Int, productTitle: String) {
        for (progress in 0..100 step 10) {
            currentProgress = progress
            updateNotification(progress, operation, productId, productTitle)
            delay(500)
        }
    }

    private fun updateNotification(progress: Int, operation: String, productId: Int, productTitle: String) {
        val notification = createNotification(progress, operation, productId, productTitle)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(progress: Int, operation: String, productId: Int, productTitle: String): android.app.Notification {
        val title = when (operation) {
            "add" -> "➕ Agregando producto"
            "edit" -> "✏️ Actualizando producto"
            "delete" -> "🗑️ Eliminando producto"
            else -> "📡 Sincronizando catálogo"
        }

        val contentText = if (productTitle.isNotEmpty()) {
            "$title: $productTitle - $progress%"
        } else {
            "$title - $progress%"
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📡 CatalogApp")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sincronización de Catálogo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación obligatoria durante la sincronización de productos"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}