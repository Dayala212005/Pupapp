package com.pdm0126.puppapp.data.model

data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val businessDisplayName: String
)
