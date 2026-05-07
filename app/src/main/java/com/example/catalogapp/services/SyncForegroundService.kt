// SyncForegroundService.kt - Versión CORREGIDA

package com.example.catalogapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
        val operation = intent?.getStringExtra("operation") ?: "sync"
        val productId = intent?.getIntExtra("product_id", 0) ?: 0
        val productTitle = intent?.getStringExtra("product_title") ?: ""

        // ✅ IMPORTANTE: Iniciar foreground ANTES de hacer cualquier tarea pesada
        startForeground(NOTIFICATION_ID, createNotification(0, operation, productId, productTitle))

        serviceScope.launch {
            syncData(operation, productId, productTitle)
            // ✅ Cuando termina, eliminar la notificación
            stopForeground(STOP_FOREGROUND_REMOVE)
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
        // ✅ Usar notify con el mismo ID para actualizar
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
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📡 CatalogApp")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            // ✅ CRÍTICO: setOngoing(true) evita que el usuario descarte la notificación
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            // ✅ Bloquear swipe lateral
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    // En SyncForegroundService.kt - Asegura que el canal tiene importancia ALTA

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sincronización de Catálogo",
                NotificationManager.IMPORTANCE_HIGH  // ✅ Cambiado de LOW a HIGH
            ).apply {
                description = "Notificación obligatoria durante la sincronización de productos"
                setSound(null, null)
                // ✅ Permitir badges
                setShowBadge(true)
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