package com.pdm0126.puppapp.data.remote.PupappAPI

import com.pdm0126.puppapp.data.dto.LoginRequest
import com.pdm0126.puppapp.data.dto.RegisterRequest
import com.pdm0126.puppapp.data.model.UserSession

interface AuthAPI {
    suspend fun login(request: LoginRequest): UserSession
    suspend fun register(request: RegisterRequest): UserSession
    suspend fun logout()
}
