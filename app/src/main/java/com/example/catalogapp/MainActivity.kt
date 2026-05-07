package com.example.catalogapp

import android.Manifest
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
import com.example.catalogapp.ui.screens.CatalogScreen
import com.example.catalogapp.ui.theme.CatalogAppTheme
import com.example.catalogapp.viewmodel.CatalogViewModel

class MainActivity : ComponentActivity() {

    // ✅ Launcher para permiso de notificaciones
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido
            android.util.Log.d("MainActivity", "✅ Permiso de notificaciones CONCEDIDO")
            Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
        } else {
            // Permiso denegado
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

        // ✅ SOLICITAR PERMISO DESPUÉS de setContent (importante)
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        // Solo para Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Verificar si ya tiene permiso
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                // ✅ Lanzar el diálogo de permiso
                android.util.Log.d("MainActivity", "🔄 Solicitando permiso de notificaciones...")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                android.util.Log.d("MainActivity", "✅ Permiso de notificaciones YA CONCEDIDO")
            }
        }
    }
}