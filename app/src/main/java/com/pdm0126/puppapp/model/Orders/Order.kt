package com.pdm0126.puppapp.model.Orders

data class Order(
    val id: String,
    val orderNumber: Int,
    val clientName: String,
    val items: List<OrderItem>,
    val createdAt: Long = System.currentTimeMillis(),
    val customTotal: Double? = null   
) {
    val total: Double
        get() = customTotal ?: items.sumOf { it.subtotal }
}
