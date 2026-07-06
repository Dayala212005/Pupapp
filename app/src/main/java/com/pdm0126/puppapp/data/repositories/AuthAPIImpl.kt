package com.pdm0126.puppapp.data.repositories

import com.pdm0126.puppapp.data.dto.AuthResponse
import com.pdm0126.puppapp.data.dto.LoginRequest
import com.pdm0126.puppapp.data.dto.RegisterRequest
import com.pdm0126.puppapp.data.dto.toModel
import com.pdm0126.puppapp.data.local.AppDatabase
import com.pdm0126.puppapp.data.model.UserSession
import com.pdm0126.puppapp.data.remote.KtorClient
import com.pdm0126.puppapp.data.remote.PupappAPI.AuthAPI
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthAPIImpl(private val database: AppDatabase) : AuthAPI {
    private val client = KtorClient.client

    override suspend fun login(request: LoginRequest): UserSession {
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status.value != 200 && response.status.value != 201) {
            throw ResponseException(response, "Login failed")
        }

        val authResponse: AuthResponse = response.body()
        val session = authResponse.toModel()
        KtorClient.sessionManager.saveSession(session.accessToken, session.refreshToken, session.businessDisplayName)
        return session
    }

    override suspend fun register(request: RegisterRequest) {
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (response.status.value != 200 && response.status.value != 201) {
            throw ResponseException(response, "Registration failed")
        }
    }

    override suspend fun logout() {
        KtorClient.sessionManager.clearSession()
        database.clearAllTables()
    }
}
