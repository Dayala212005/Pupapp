package com.pdm0126.puppapp.model.Auth

data class LoginRequest(
    val sessionName: String,
    val password: String
)