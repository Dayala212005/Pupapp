package com.pdm0126.puppapp.model.Menu

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val isPredefined: Boolean = false,
    val isCustom: Boolean = false
)
