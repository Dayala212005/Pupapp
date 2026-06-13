package com.pdm0126.puppapp.screens.authorView.loginview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdm0126.puppapp.data.dto.LoginRequest
import com.pdm0126.puppapp.data.remote.PupappAPI.AuthAPI
import com.pdm0126.puppapp.data.repositories.AuthAPIImpl
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authAPI: AuthAPI = AuthAPIImpl()
) : ViewModel() {

    var sessionName by mutableStateOf("")
    var password by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var loginSuccess by mutableStateOf(false)
        private set

    fun resetLoginState() {
        loginSuccess = false
    }

    fun onLoginClick() {
        if (sessionName.isBlank() || password.isBlank()) {
            errorMessage = "Por favor, completa todos los campos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                authAPI.login(LoginRequest(sessionName, password))
                loginSuccess = true
            } catch (e: Exception) {
                errorMessage = "Error al iniciar sesión: ${e.localizedMessage ?: "Error desconocido"}"
            } finally {
                isLoading = false
            }
        }
    }
}
