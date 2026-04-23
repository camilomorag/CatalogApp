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
import com.example.catalogapp.model.CartResponse
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

    var cartsFromApi by mutableStateOf<List<CartResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var lastRequest by mutableStateOf("")
        private set

    var lastResponse by mutableStateOf("")
        private set

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                products = repository.fetchAllProducts()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "No se pudieron cargar los productos: ${e.localizedMessage}"
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

    fun clearMessages() {
        errorMessage = null
        successMessage = null
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

                lastRequest = """
POST /carts

Body:
$request
            """.trimIndent()

                val response = repository.createCart(request)

                println("RESPUESTA API: $response")

                lastResponse = """
Response:
$response
            """.trimIndent()

                successMessage = "Compra enviada correctamente. ID carrito: ${response.id}"
                clearCart()

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al enviar carrito: ${e.localizedMessage}"
                lastResponse = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadCartsFromApi() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                cartsFromApi = repository.getCarts()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error al obtener carritos: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

}
