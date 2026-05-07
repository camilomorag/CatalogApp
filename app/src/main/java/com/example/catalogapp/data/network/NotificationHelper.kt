package com.example.catalogapp.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.catalogapp.MainActivity
import com.example.catalogapp.model.Product

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_SUCCESS = "catalog_channel_success"
        private const val CHANNEL_ID_ERROR = "catalog_channel_error"
        private const val CHANNEL_ID_CART = "catalog_channel_cart"
        private const val CHANNEL_ID_PRODUCTS = "catalog_channel_products"

        private const val NOTIFICATION_ID_SUCCESS = 2001
        private const val NOTIFICATION_ID_ERROR = 2002
        private const val NOTIFICATION_ID_CART = 2003
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Canal de Éxito
            val channelSuccess = NotificationChannel(
                CHANNEL_ID_SUCCESS,
                "Operaciones exitosas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando una operación es exitosa"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }

            // Canal de Error (Alta prioridad)
            val channelError = NotificationChannel(
                CHANNEL_ID_ERROR,
                "Errores",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando ocurre un error"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 300, 500)
            }

            // Canal de Carrito
            val channelCart = NotificationChannel(
                CHANNEL_ID_CART,
                "Carrito de compras",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones relacionadas con el carrito"
                enableVibration(true)
            }

            // Canal de Productos
            val channelProducts = NotificationChannel(
                CHANNEL_ID_PRODUCTS,
                "Productos destacados",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones con información de productos"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channelSuccess)
            notificationManager.createNotificationChannel(channelError)
            notificationManager.createNotificationChannel(channelCart)
            notificationManager.createNotificationChannel(channelProducts)
        }
    }

    private fun getPendingIntent(extraData: String? = null): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            extraData?.let { putExtra("notification_data", it) }
        }

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ========== NOTIFICACIÓN DE BIENVENIDA ==========
    fun showWelcomeNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SUCCESS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Bienvenido a CatalogApp!")
            .setContentText("Explora productos, agrega al carrito y realiza tus compras")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("¡Bienvenido a CatalogApp!\n\nExplora productos, agrega al carrito y realiza tus compras"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent())
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_SUCCESS + 100, notification)
    }

    // ========== NOTIFICACIÓN DE ÉXITO ==========
    fun showSuccessNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SUCCESS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$title\n\n$message"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent())
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_SUCCESS, notification)
    }

    // ========== NOTIFICACIÓN DE ERROR ==========
    fun showErrorNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ERROR)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("❌ $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("❌ $title\n\n$message"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent())
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_ERROR, notification)
    }

    // ========== NOTIFICACIÓN DE CARRITO (COMPRA) ==========
    fun showCartNotification(cartId: Int, totalProducts: Int, totalAmount: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CART)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("🛒 ¡Compra realizada!")
            .setContentText("Has comprado $totalProducts productos por $${String.format("%.2f", totalAmount)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🛒 ¡COMPRA EXITOSA!\n\nHas comprado $totalProducts productos\nTotal: $${String.format("%.2f", totalAmount)}\nID del carrito: $cartId\n\n¡Gracias por tu compra!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent("cart_id=$cartId"))
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_CART, notification)
    }

    // ========== PLANTILLA 1: Producto + Precio ==========
    fun notifyPlantilla1(product: Product) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRODUCTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📦 Oferta relámpago")
            .setContentText(product.title)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("📦 ${product.title}\n\n💲 Precio: $${String.format("%.2f", product.price)}\n\n🔥 ¡Oferta por tiempo limitado!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(product.id.toString()))
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(3001 + product.id, notification)
    }

    // ========== PLANTILLA 2: Producto + Precio + Imagen ==========
    fun notifyPlantilla2(product: Product) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRODUCTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🛍️ Te podría interesar")
            .setContentText(product.title)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🛍️ ${product.title}\n\n💲 Precio: $${String.format("%.2f", product.price)}\n\n📷 ¡Mira cómo se ve este producto!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(product.id.toString()))
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(3002 + product.id, notification)
    }

    // ========== PLANTILLA 3: Producto + Categoría + Descripción ==========
    fun notifyPlantilla3(product: Product) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PRODUCTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🏷️ Nuevo en ${product.category ?: "Catálogo"}")
            .setContentText(product.title)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🏷️ ${product.title}\n\n📂 ${product.category ?: "Sin categoría"}\n\n📝 ${product.description.take(80)}..."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(product.id.toString()))
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(3003 + product.id, notification)
    }
}