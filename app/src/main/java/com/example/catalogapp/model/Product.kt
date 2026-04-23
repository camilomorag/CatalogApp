package com.example.catalogapp.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String?,  // Categoría del producto
    val image: String,
    val rating: Rating?      // Rating opcional
)

data class Rating(
    val rate: Double,
    val count: Int
)