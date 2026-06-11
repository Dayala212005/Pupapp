package com.pdm0126.puppapp.data.dto

import com.pdm0126.puppapp.data.model.OrderItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDTO(
    val id: Int,
    @SerialName("order_id") val orderId: Int,
    @SerialName("product_name") val productName: String,
    val quantity: Int,
    @SerialName("price_at_time") val priceAtTime: Double
)

fun OrderItemDTO.toModel() = OrderItem(
    id = id,
    orderId = orderId,
    productName = productName,
    quantity = quantity,
    pricePerDish = priceAtTime
)
