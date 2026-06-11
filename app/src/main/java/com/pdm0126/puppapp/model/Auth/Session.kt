package com.pdm0126.puppapp.model.Auth

data class Session(
    val id: String,
    val businessName: String,
    val sessionName: String,
    val token: String
)