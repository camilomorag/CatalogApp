package com.example.catalogapp.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogapp.data.network.NotificationHelper
import com.example.catalogapp.data.network.RetrofitClient
import com.example.catalogapp.data.repository.ProductRepository
import com.example.catalogapp.model.CartProduct
import com.example.catalogapp.model.CartRequest
import com.example.catalogapp.model.Product
import com.example.catalogapp.services.SyncForegroundService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(RetrofitClient.apiService)
    private val notificationHelper = NotificationHelper(getApplication())

    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    var cart by mutableStateOf<List<Product>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var apiLogs by mutableStateOf<List<String>>(emptyList())
        private set

    private var welcomeShown = false

    init {
        loadProducts()
        if (!welcomeShown) {
            notificationHelper.showWelcomeNotification()
            welcomeShown = true
        }
    }

    private fun addApiLog(title: String, request: String, response: String) {
        val log = """
$title

REQUEST:
$request

RESPONSE:
$response
        """.trimIndent()
        apiLogs = listOf(log) + apiLogs
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun loadProducts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.fetchAllProducts()
                products = result
                addApiLog(
                    title = "GET /products",
                    request = "GET https://fakestoreapi.com/products",
                    response = "Productos obtenidos: ${result.size}"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "No se pudieron cargar los productos: ${e.localizedMessage}"
                addApiLog(
                    title = "GET /products",
                    request = "GET https://fakestoreapi.com/products",
                    response = "ERROR: ${e.localizedMessage}"
                )
                notificationHelper.showErrorNotification(
                    "Error de conexión",
                    "No se pudieron cargar los productos. Verifica tu conexión a internet."
                )
            } finally {
                isLoading = false
            }
        }
    }

    // ========== FUNCIÓN START SYNC (CORREGIDA - recibe Product) ==========
    fun startSync(operation: String, product: Product? = null) {
        val intent = Intent(getApplication(), SyncForegroundService::class.java)
        intent.putExtra("operation", operation)
        if (product != null) {
            intent.putExtra("product_id", product.id)
            intent.putExtra("product_title", product.title)
        }
        getApplication<Application>().startService(intent)
    }

    fun addProduct(product: Product) {
        startSync("add", product)  // ✅ PASA EL PRODUCTO COMPLETO
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val created = repository.createProduct(product)
                products = listOf(created) + products
                successMessage = "Producto agregado correctamente"
                addApiLog(
                    title = "POST /products",
                    request = "POST https://fakestoreapi.com/products\nBody: $product",
                    response = created.toString()
                )
                notificationHelper.showSuccessNotification(
                    "✅ Producto agregado",
                    "${created.title} se ha agregado al catálogo"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al agregar producto: ${e.localizedMessage}"
                addApiLog(
                    title = "POST /products",
                    request = "POST https://fakestoreapi.com/products\nBody: $product",
                    response = "ERROR: ${e.localizedMessage}"
                )
                notificationHelper.showErrorNotification(
                    "Error al agregar",
                    "No se pudo agregar ${product.title}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProduct(product: Product) {
        startSync("edit", product)  // ✅ PASA EL PRODUCTO COMPLETO
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val updated = repository.updateProduct(product.id, product)
                products = products.map {
                    if (it.id == product.id) updated else it
                }
                successMessage = "Producto editado correctamente"
                addApiLog(
                    title = "PUT /products/${product.id}",
                    request = "PUT https://fakestoreapi.com/products/${product.id}\nBody: $product",
                    response = updated.toString()
                )
                notificationHelper.showSuccessNotification(
                    "✏️ Producto actualizado",
                    "${updated.title} se ha modificado correctamente"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al editar producto: ${e.localizedMessage}"
                addApiLog(
                    title = "PUT /products/${product.id}",
                    request = "PUT https://fakestoreapi.com/products/${product.id}\nBody: $product",
                    response = "ERROR: ${e.localizedMessage}"
                )
                notificationHelper.showErrorNotification(
                    "Error al editar",
                    "No se pudo modificar ${product.title}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteProduct(productId: Int) {
        val product = products.find { it.id == productId }
        startSync("delete", product)  // ✅ PASA EL PRODUCTO COMPLETO
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            val productTitle = products.find { it.id == productId }?.title ?: "Producto"
            try {
                val deleted = repository.deleteProduct(productId)
                products = products.filterNot { it.id == productId }
                cart = cart.filterNot { it.id == productId }
                successMessage = "Producto eliminado correctamente"
                addApiLog(
                    title = "DELETE /products/$productId",
                    request = "DELETE https://fakestoreapi.com/products/$productId",
                    response = deleted.toString()
                )
                notificationHelper.showSuccessNotification(
                    "🗑️ Producto eliminado",
                    "$productTitle se ha eliminado del catálogo"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al eliminar producto: ${e.localizedMessage}"
                addApiLog(
                    title = "DELETE /products/$productId",
                    request = "DELETE https://fakestoreapi.com/products/$productId",
                    response = "ERROR: ${e.localizedMessage}"
                )
                notificationHelper.showErrorNotification(
                    "Error al eliminar",
                    "No se pudo eliminar $productTitle"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun addToCart(product: Product) {
        cart = cart + product
        successMessage = "${product.title} agregado al carrito"
        if (cart.size == 1 || cart.size % 3 == 0) {
            notificationHelper.showSuccessNotification(
                "🛒 Producto agregado",
                "${product.title} - Total en carrito: ${cart.size} productos"
            )
        }
    }

    fun removeFromCart(product: Product) {
        val mutableCart = cart.toMutableList()
        val index = mutableCart.indexOfFirst { it.id == product.id }
        if (index != -1) {
            mutableCart.removeAt(index)
            cart = mutableCart
            successMessage = "${product.title} eliminado del carrito"
        }
    }

    fun cartCount(): Int = cart.size
    fun cartTotal(): Double = cart.sumOf { it.price }
    fun clearCart() {
        cart = emptyList()
    }

    fun sendCartToApi(userId: Int = 1) {
        if (cart.isEmpty()) {
            errorMessage = "El carrito está vacío"
            notificationHelper.showErrorNotification(
                "Carrito vacío",
                "Agrega productos antes de comprar"
            )
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val groupedProducts = cart
                    .groupingBy { it.id }
                    .eachCount()
                    .map { (productId, quantity) ->
                        CartProduct(productId = productId, quantity = quantity)
                    }
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val request = CartRequest(
                    userId = userId,
                    date = currentDate,
                    products = groupedProducts
                )
                val response = repository.createCart(request)
                addApiLog(
                    title = "POST /carts",
                    request = "POST https://fakestoreapi.com/carts\nBody: $request",
                    response = response.toString()
                )
                successMessage = "Compra enviada correctamente. ID carrito: ${response.id}"
                notificationHelper.showCartNotification(
                    cartId = response.id,
                    totalProducts = cart.size,
                    totalAmount = cartTotal()
                )
                clearCart()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al enviar carrito: ${e.localizedMessage}"
                addApiLog(
                    title = "POST /carts",
                    request = "POST https://fakestoreapi.com/carts",
                    response = "ERROR: ${e.localizedMessage}"
                )
                notificationHelper.showErrorNotification(
                    "Error en la compra",
                    "No se pudo procesar tu pedido: ${e.localizedMessage}"
                )
            } finally {
                isLoading = false
            }
        }
    }
}