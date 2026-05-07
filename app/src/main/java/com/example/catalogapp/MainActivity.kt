package com.example.catalogapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.catalogapp.services.CartNotificationService
import com.example.catalogapp.ui.screens.CatalogScreen
import com.example.catalogapp.ui.theme.CatalogAppTheme
import com.example.catalogapp.viewmodel.CatalogViewModel

class MainActivity : ComponentActivity() {

    // ✅ Launcher para permiso de notificaciones
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "✅ Permiso de notificaciones CONCEDIDO")
            Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
        } else {
            android.util.Log.w("MainActivity", "❌ Permiso de notificaciones DENEGADO")
            Toast.makeText(
                this,
                "Las notificaciones no funcionarán correctamente. Ve a Configuración para activarlas.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Manejar acciones desde notificaciones (COMPRAR o DESCARTAR)
        intent?.let { handleNotificationIntent(it) }

        // ✅ Inicializar ViewModel
        val viewModel = CatalogViewModel(application)

        setContent {
            CatalogAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CatalogScreen(viewModel = viewModel)
                }
            }
        }

        // ✅ SOLICITAR PERMISO DESPUÉS de setContent
        requestNotificationPermission()
    }

    // ✅ CORREGIDO: onNewIntent con la firma correcta y parámetro no nullable
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // ✅ Manejar nuevas intents cuando la app ya está abierta
        handleNotificationIntent(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                android.util.Log.d("MainActivity", "🔄 Solicitando permiso de notificaciones...")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                android.util.Log.d("MainActivity", "✅ Permiso de notificaciones YA CONCEDIDO")
            }
        }
    }

    // ========== ✅ Manejar acciones de la notificación obligatoria ==========
    // ✅ CORREGIDO: parámetro no nullable
    private fun handleNotificationIntent(intent: Intent) {
        when (intent.action) {
            // ✅ Acción COMPRAR
            CartNotificationService.ACTION_BUY -> {
                val productId = intent.getIntExtra(CartNotificationService.EXTRA_PRODUCT_ID, 0)
                val productTitle = intent.getStringExtra(CartNotificationService.EXTRA_PRODUCT_TITLE) ?: "Producto"
                val productPrice = intent.getDoubleExtra(CartNotificationService.EXTRA_PRODUCT_PRICE, 0.0)

                android.util.Log.d("MainActivity", "🛒 Comprar producto: $productTitle (ID: $productId)")

                Toast.makeText(
                    this,
                    "Procediendo a comprar: $productTitle - $${productPrice}",
                    Toast.LENGTH_LONG
                ).show()

                // ✅ Detener la notificación
                stopService(Intent(this, CartNotificationService::class.java))
            }

            // ✅ Acción DESCARTAR
            CartNotificationService.ACTION_DISMISS -> {
                val productId = intent.getIntExtra(CartNotificationService.EXTRA_PRODUCT_ID, 0)
                val productTitle = intent.getStringExtra(CartNotificationService.EXTRA_PRODUCT_TITLE) ?: "Producto"

                android.util.Log.d("MainActivity", "🗑️ Descartar producto: $productTitle (ID: $productId)")

                Toast.makeText(
                    this,
                    "Producto descartado: $productTitle",
                    Toast.LENGTH_SHORT
                ).show()

                // ✅ Detener la notificación
                stopService(Intent(this, CartNotificationService::class.java))
            }
        }
    }
}