package com.pdm0126.puppapp.data.model

data class Product(
    val id: Int,
    val restaurantId: Int,
    val name: String,
    val priceBase: Double,
    val category: String?,
    val imageUrl: String?
)