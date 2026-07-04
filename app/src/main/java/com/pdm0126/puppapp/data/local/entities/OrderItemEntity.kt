package com.pdm0126.puppapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdm0126.puppapp.data.model.OrderItem

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val pricePerDish: Double
)

fun OrderItemEntity.toModel() = OrderItem(
    id = id,
    orderId = orderId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    pricePerDish = pricePerDish
)

fun OrderItem.toEntity() = OrderItemEntity(
    id = id,
    orderId = orderId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    pricePerDish = pricePerDish
)
