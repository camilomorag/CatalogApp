package com.example.catalogapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogapp.data.network.RetrofitClient
import com.example.catalogapp.data.repository.ProductRepository
import com.example.catalogapp.model.CartProduct
import com.example.catalogapp.model.CartRequest
import com.example.catalogapp.model.Product
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CatalogViewModel : ViewModel() {

    private val repository = ProductRepository(RetrofitClient.apiService)

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

    init {
        loadProducts()
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
            } finally {
                isLoading = false
            }
        }
    }

    fun addProduct(product: Product) {
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
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al agregar producto: ${e.localizedMessage}"

                addApiLog(
                    title = "POST /products",
                    request = "POST https://fakestoreapi.com/products\nBody: $product",
                    response = "ERROR: ${e.localizedMessage}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProduct(product: Product) {
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
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al editar producto: ${e.localizedMessage}"

                addApiLog(
                    title = "PUT /products/${product.id}",
                    request = "PUT https://fakestoreapi.com/products/${product.id}\nBody: $product",
                    response = "ERROR: ${e.localizedMessage}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

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
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al eliminar producto: ${e.localizedMessage}"

                addApiLog(
                    title = "DELETE /products/$productId",
                    request = "DELETE https://fakestoreapi.com/products/$productId",
                    response = "ERROR: ${e.localizedMessage}"
                )
            } finally {
                isLoading = false
            }
        }
    }

    fun addToCart(product: Product) {
        cart = cart + product
    }

    fun removeFromCart(product: Product) {
        val mutableCart = cart.toMutableList()
        val index = mutableCart.indexOfFirst { it.id == product.id }
        if (index != -1) {
            mutableCart.removeAt(index)
            cart = mutableCart
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

                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date())

                val request = CartRequest(
                    userId = userId,
                    date = currentDate,
                    products = groupedProducts
                )

                val response = repository.createCart(request)

                println("RESPUESTA API: $response")

                addApiLog(
                    title = "POST /carts",
                    request = "POST https://fakestoreapi.com/carts\nBody: $request",
                    response = response.toString()
                )

                successMessage = "Compra enviada correctamente. ID carrito: ${response.id}"
                clearCart()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al enviar carrito: ${e.localizedMessage}"

                addApiLog(
                    title = "POST /carts",
                    request = "POST https://fakestoreapi.com/carts",
                    response = "ERROR: ${e.localizedMessage}"
                )
            } finally {
                isLoading = false
            }
        }
    }
}