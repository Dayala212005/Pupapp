package com.pdm0126.puppapp.screens.authorView.registrerView

import androidx.lifecycle.ViewModel
import com.pdm0126.puppapp.data.dto.RegisterRequest
import com.pdm0126.puppapp.data.remote.PupappAPI.AuthAPI
import com.pdm0126.puppapp.data.repositories.AuthAPIImpl
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch
class RegisterViewModel(
    private val authAPI: AuthAPI = AuthAPIImpl()
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
        android.util.Log.d("RegisterVM", "businessName=${_businessName.value}, sessionName=${_sessionName.value}")

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
                authAPI.register(RegisterRequest( _sessionName.value, _password.value,_businessName.value,),)
                _registerSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrarse: ${e.localizedMessage ?: "Error desconocido"}"
                android.util.Log.e("RegisterVM", "Error: ${e.localizedMessage}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}