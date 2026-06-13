package com.pdm0126.puppapp.data.dto

import com.pdm0126.puppapp.data.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
    val id: Int,
    @SerialName("restaurant_id") val restaurantId: Int,
    val name: String,
    @SerialName("price_base") val priceBase: String,
    val category: String?,
    @SerialName("image_url") val imageUrl: String? = null
)

fun ProductDTO.toModel() = Product(
    id = id,
    restaurantId = restaurantId,
    name = name,
    priceBase = priceBase.toDoubleOrNull() ?: 0.0,
    category = category,
    imageUrl = imageUrl
)
