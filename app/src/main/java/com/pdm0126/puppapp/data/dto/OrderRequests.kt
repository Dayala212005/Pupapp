package com.pdm0126.puppapp.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    @SerialName("order_reference") val orderReference: String?,
    @SerialName("customer_name") val customerName: String?,
    @SerialName("final_total") val finalTotal: Double?,
    @SerialName("total_adjustment_note") val totalAdjustmentNote: String?,
    val items: List<CreateOrderItemRequest>
)

@Serializable
data class CreateOrderItemRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class UpdateOrderStatusRequest(
    @SerialName("status_id") val statusId: Int
)
