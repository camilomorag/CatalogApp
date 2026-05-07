package com.example.catalogapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable  // ✅ Agregar este import

data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String?,
    val image: String,
    val rating: Rating?
) : Serializable  // ✅ Implementar Serializable

data class Rating(
    val rate: Double,
    val count: Int
) : Serializable  // ✅ También Rating