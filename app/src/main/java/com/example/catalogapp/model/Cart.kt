package com.example.catalogapp.model

data class CartRequest(
    val userId: Int,
    val date: String,
    val products: List<CartProduct>
)

data class CartResponse(
    val id: Int,
    val userId: Int,
    val date: String,
    val products: List<CartProduct>
)

data class CartProduct(
    val productId: Int,
    val quantity: Int
)