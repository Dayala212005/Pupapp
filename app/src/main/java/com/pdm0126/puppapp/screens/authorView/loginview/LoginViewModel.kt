package com.pdm0126.puppapp.screens.authorView.loginview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.puppapp.PupappApplication
import com.pdm0126.puppapp.data.dto.LoginRequest
import com.pdm0126.puppapp.data.remote.PupappAPI.AuthAPI
import com.pdm0126.puppapp.utils.toUserFriendlyMessage
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authAPI: AuthAPI
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
                errorMessage = e.toUserFriendlyMessage(isLogin = true)
            } finally {
                isLoading = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PupappApplication
                LoginViewModel(app.appProvider.provideAuthRepository())
            }
        }
    }
}
