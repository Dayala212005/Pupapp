package com.pdm0126.puppapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.pdm0126.puppapp.data.model.Order
import com.pdm0126.puppapp.data.model.OrderItem
import java.util.Date

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Int,
    val restaurantId: Int,
    val statusId: Int,
    val orderNumber: Int,
    val orderReference: String?,
    val customerName: String?,
    val calculatedSubtotal: Double,
    val finalTotal: Double,
    val totalAdjustmentNote: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    // Campos para sincronización manual
    val isSynced: Boolean = true,
    val pendingAction: String? = null // "CREATE", "UPDATE", "DELETE"
)

data class OrderWithItems(
    @androidx.room.Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)

fun OrderWithItems.toModel() = order.toModel(items.map { it.toModel() })

fun OrderEntity.toModel(items: List<OrderItem> = emptyList()) = Order(
    id = id,
    restaurantId = restaurantId,
    statusId = statusId,
    orderNumber = orderNumber,
    orderReference = orderReference,
    customerName = customerName,
    calculatedSubtotal = calculatedSubtotal,
    finalTotal = finalTotal,
    totalAdjustmentNote = totalAdjustmentNote,
    createdAt = createdAt,
    updatedAt = updatedAt,
    items = items,
    // Podrías añadir isSynced al modelo Order si quieres mostrar un icono en la UI
)

fun Order.toEntity(isSynced: Boolean = true, pendingAction: String? = null) = OrderEntity(
    id = id,
    restaurantId = restaurantId,
    statusId = statusId,
    orderNumber = orderNumber,
    orderReference = orderReference,
    customerName = customerName,
    calculatedSubtotal = calculatedSubtotal,
    finalTotal = finalTotal,
    totalAdjustmentNote = totalAdjustmentNote,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    pendingAction = pendingAction
)
