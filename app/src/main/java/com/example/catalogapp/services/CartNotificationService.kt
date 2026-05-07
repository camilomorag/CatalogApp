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

class CartNotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "cart_obligatory_channel"
        private const val NOTIFICATION_ID = 2001

        // Acciones
        const val ACTION_BUY = "com.example.catalogapp.BUY"
        const val ACTION_DISMISS = "com.example.catalogapp.DISMISS"

        // Extras
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_PRODUCT_TITLE = "product_title"
        const val EXTRA_PRODUCT_PRICE = "product_price"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val productId = intent?.getIntExtra(EXTRA_PRODUCT_ID, 0) ?: 0
        val productTitle = intent?.getStringExtra(EXTRA_PRODUCT_TITLE) ?: "Producto"
        val productPrice = intent?.getDoubleExtra(EXTRA_PRODUCT_PRICE, 0.0) ?: 0.0

        createNotificationChannel()

        val notification = createObligatoryNotification(productId, productTitle, productPrice)

        // ✅ Iniciar foreground con notificación OBLIGATORIA
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    private fun createObligatoryNotification(
        productId: Int,
        productTitle: String,
        productPrice: Double
    ): android.app.Notification {

        // Intent para acción COMPRAR
        val buyIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_BUY
            putExtra(EXTRA_PRODUCT_ID, productId)
            putExtra(EXTRA_PRODUCT_TITLE, productTitle)
            putExtra(EXTRA_PRODUCT_PRICE, productPrice)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val buyPendingIntent = PendingIntent.getActivity(
            this,
            productId,
            buyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent para acción DESCARTAR (cierra el servicio)
        val dismissIntent = Intent(this, CartNotificationService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_PRODUCT_ID, productId)
        }

        val dismissPendingIntent = PendingIntent.getService(
            this,
            productId + 1000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🛒 Producto agregado al carrito")
            .setContentText("$productTitle - $${productPrice}")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("¿Qué deseas hacer con $productTitle?")
            )
            // ✅ Botón COMPRAR (usando icono que SÍ existe)
            .addAction(
                android.R.drawable.ic_menu_save,  // ✅ ICONO CORREGIDO
                "Comprar ahora",
                buyPendingIntent
            )
            // ✅ Botón DESCARTAR (usando icono que SÍ existe)
            .addAction(
                android.R.drawable.ic_menu_delete,  // ✅ Este sí existe
                "Descartar",
                dismissPendingIntent
            )
            // ✅ CRÍTICO: Hace la notificación OBLIGATORIA (no descartable)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones del Carrito",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones obligatorias para acciones del carrito"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}