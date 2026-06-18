package com.pdm0126.puppapp.data.model

import java.util.Date

data class Order(
    val id: Int,
    val restaurantId: Int,
    val statusId: Int, //1: Pendiente 2: Preparado 3:Listo 4:Entregado 5:Cancelado
    val orderNumber: Int,
    val orderReference: String?,
    val customerName: String?,
    val calculatedSubtotal: Double,
    val finalTotal: Double,
    val totalAdjustmentNote: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val items: List<OrderItem> = emptyList()
)