package com.pdm0126.puppapp.data.dto

import com.pdm0126.puppapp.data.model.UserSession
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("access_name") val accessName: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    @SerialName("access_name") val accessName: String,
    val password: String,
    @SerialName("business_display_name") val businessDisplayName: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    @SerialName("business_display_name") val businessDisplayName: String
)

fun AuthResponse.toModel() = UserSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    businessDisplayName = businessDisplayName
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class RefreshResponse(
    val accessToken: String
)
