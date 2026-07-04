package com.pdm0126.puppapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pdm0126.puppapp.data.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val restaurantId: Int,
    val name: String,
    val priceBase: Double,
    val category: String?,
    val imageUrl: String?,
    val isSynced: Boolean = true,
    val pendingAction: String? = null
)

fun ProductEntity.toModel() = Product(
    id = id,
    restaurantId = restaurantId,
    name = name,
    priceBase = priceBase,
    category = category,
    imageUrl = imageUrl
)

fun Product.toEntity(isSynced: Boolean = true, pendingAction: String? = null) = ProductEntity(
    id = id,
    restaurantId = restaurantId,
    name = name,
    priceBase = priceBase,
    category = category,
    imageUrl = imageUrl,
    isSynced = isSynced,
    pendingAction = pendingAction
)
