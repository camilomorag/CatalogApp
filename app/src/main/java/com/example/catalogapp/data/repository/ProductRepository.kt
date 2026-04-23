package com.example.catalogapp.data.repository

import com.example.catalogapp.data.network.ProductApiService
import com.example.catalogapp.model.Product

class ProductRepository(private val api: ProductApiService) {

    suspend fun fetchAllProducts(): List<Product> {
        return api.getProducts()
    }

    suspend fun createProduct(product: Product): Product {
        return api.createProduct(product)
    }

    suspend fun updateProduct(id: Int, product: Product): Product {
        return api.updateProduct(id, product)
    }

    suspend fun deleteProduct(id: Int): Product {
        return api.deleteProduct(id)
    }
}