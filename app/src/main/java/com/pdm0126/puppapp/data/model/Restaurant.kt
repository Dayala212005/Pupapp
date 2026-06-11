package com.pdm0126.puppapp.data.model

import java.util.Date

data class Restaurant(
    val id: Int,
    val accessName: String,
    val businessDisplayName: String?,
    val createdAt: Date? = null
)