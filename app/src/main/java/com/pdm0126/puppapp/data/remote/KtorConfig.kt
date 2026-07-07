package com.pdm0126.puppapp.data.remote

import com.pdm0126.puppapp.data.dto.RefreshRequest
import com.pdm0126.puppapp.data.dto.RefreshResponse
import com.pdm0126.puppapp.data.local.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClient {
    const val BASE_URL = "https://pupapp-api.vercel.app"
    lateinit var sessionManager: SessionManager
    var onSessionExpired: (suspend () -> Unit)? = null

    private var _client: HttpClient? = null

    val client: HttpClient
        get() = _client ?: synchronized(this) {
            _client ?: createClient().also { _client = it }
        }

    fun resetClient() = synchronized(this) {
        _client?.close()
        _client = null
    }

    private fun createClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }

            install(Logging) {
                level = LogLevel.ALL
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = sessionManager.getAccessToken()
                        val refreshToken = sessionManager.getRefreshToken()
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val refreshToken = sessionManager.getRefreshToken() ?: return@refreshTokens null
                        
                        try {
                            val refreshClient = HttpClient(OkHttp) {
                                install(ContentNegotiation) { 
                                    json(Json { ignoreUnknownKeys = true }) 
                                }
                            }
                            
                            val response = refreshClient.post("$BASE_URL/api/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshRequest(refreshToken))
                            }

                            if (response.status.value == 200 || response.status.value == 201) {
                                val body = response.body<RefreshResponse>()
                                sessionManager.updateTokens(body.accessToken, refreshToken)
                                BearerTokens(body.accessToken, refreshToken)
                            } else if (response.status.value == 401 || response.status.value == 403) {
                                onSessionExpired?.invoke()
                                null
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
            }

            defaultRequest {
                url(BASE_URL)
                header(HttpHeaders.Accept, "application/json")
            }
        }
    }
}
