package com.example.catalogapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogapp.data.network.RetrofitClient
import com.example.catalogapp.data.repository.ProductRepository
import com.example.catalogapp.model.Product
import kotlinx.coroutines.launch

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
                errorMessage = "No se pudieron cargar los productos: ${e.message}"
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

    fun cartCount(): Int {
        return cart.size
    }

    fun cartTotal(): Double {
        return cart.sumOf { it.price }
    }

    fun clearCart() {
        cart = emptyList()
    }

    fun clearError() {
        errorMessage = null
    }
}