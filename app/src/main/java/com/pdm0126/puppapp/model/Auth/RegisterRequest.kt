package com.pdm0126.puppapp.model.Auth

data class RegisterRequest(
    val businessName: String,
    val sessionName: String,
    val password: String
)