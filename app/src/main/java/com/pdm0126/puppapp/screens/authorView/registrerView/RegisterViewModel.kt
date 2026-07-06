package com.pdm0126.puppapp.screens.authorView.registrerView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.pdm0126.puppapp.data.dto.RegisterRequest
import com.pdm0126.puppapp.data.remote.PupappAPI.AuthAPI
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.puppapp.PupappApplication
import com.pdm0126.puppapp.utils.toUserFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class RegisterViewModel(
    private val authAPI: AuthAPI
) : ViewModel() {

    private val _businessName = MutableStateFlow("")
    val businessName = _businessName.asStateFlow()

    private val _sessionName = MutableStateFlow("")
    val sessionName = _sessionName.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible = _passwordVisible.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess = _registerSuccess.asStateFlow()

    fun onBusinessNameChange(value: String)    { _businessName.value = value }
    fun onSessionNameChange(value: String)     { _sessionName.value = value }
    fun onPasswordChange(value: String)        { _password.value = value }
    fun onConfirmPasswordChange(value: String) { _confirmPassword.value = value }
    fun onPasswordVisibleToggle()              { _passwordVisible.value = !_passwordVisible.value }

    fun resetRegisterState() {
        _registerSuccess.value = false
    }

    fun onRegisterClick() {
        if (_isLoading.value) return

        if (_businessName.value.isBlank() || _sessionName.value.isBlank() ||
            _password.value.isBlank() || _confirmPassword.value.isBlank()
        ) {
            _errorMessage.value = "Por favor, completa todos los campos"
            return
        }

        if (_password.value != _confirmPassword.value) {
            _errorMessage.value = "Las contraseñas no coinciden"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                authAPI.register(RegisterRequest( _sessionName.value, _password.value,_businessName.value,))
                _registerSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.toUserFriendlyMessage(isRegister = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PupappApplication
                RegisterViewModel(app.appProvider.provideAuthRepository())
            }
        }
    }
}
