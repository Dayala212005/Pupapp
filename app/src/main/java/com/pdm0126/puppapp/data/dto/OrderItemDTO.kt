package com.pdm0126.puppapp.data.dto

import com.pdm0126.puppapp.data.model.OrderItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDTO(
    val id: Int,
    val orderId: Int? = null,
    val productId: Int? = null,
    val productName: String? = null,
    val quantity: Int,
    @SerialName("price_per_dish") val pricePerDish: String
)

fun OrderItemDTO.toModel() = OrderItem(
    id = id,
    orderId = orderId ?: 0,
    productId = productId ?: 0,
    productName = productName ?: "Producto",
    quantity = quantity,
    pricePerDish = pricePerDish.toDoubleOrNull() ?: 0.0
)
