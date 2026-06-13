package com.pdm0126.puppapp.data.dto

import com.pdm0126.puppapp.data.model.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable
data class OrderDTO(
    val id: Int,
    @SerialName("restaurant_id") val restaurantId: Int,
    @SerialName("status_id") val statusId: Int, //1: Pendiente 2: Preparado 3:Listo 4:Entregado 5:Cancelado
    @SerialName("order_reference") val orderReference: String?,
    @SerialName("customer_name") val customerName: String?,
    @SerialName("calculated_subtotal") val calculatedSubtotal: String? = "0.00",
    @SerialName("final_total") val finalTotal: String,
    @SerialName("total_adjustment_note") val totalAdjustmentNote: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val items: List<OrderItemDTO> = emptyList()
)

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun String.toDate(): Date? = try {
    apiDateFormat.parse(this)
} catch (e: Exception) {
    null
}

fun OrderDTO.toModel(): Order {
    return Order(
        id = id,
        restaurantId = restaurantId,
        statusId = statusId,
        orderReference = orderReference,
        customerName = customerName,
        calculatedSubtotal = calculatedSubtotal?.toDoubleOrNull() ?: 0.0,
        finalTotal = finalTotal.toDoubleOrNull() ?: 0.0,
        totalAdjustmentNote = totalAdjustmentNote,
        createdAt = createdAt.toDate(),
        updatedAt = updatedAt.toDate(),
        items = items.map { it.toModel() }
    )
}
