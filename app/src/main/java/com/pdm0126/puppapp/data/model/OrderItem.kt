package com.pdm0126.puppapp.data.model

data class OrderItem(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val pricePerDish: Double
)