package com.example.yungasdistribuidora.presentation.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.LoginRequest
import com.example.yungasdistribuidora.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class RequiresTwoFactor(val challengeToken: String) : LoginUiState
    data class RequiresTwoFactorSetup(val challengeToken: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(emailInput: String, passwordInput: String) {
        val email = emailInput.trim()
        val password = passwordInput

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginUiState.Error("Por favor ingrese un correo electrónico válido.")
            return
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.Error("La contraseña no puede estar vacía.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = authRepository.login(LoginRequest(email, password))
            result.fold(
                onSuccess = { response ->
                    when {
                        response.requiresTwoFactorSetup == true && !response.challengeToken.isNullOrBlank() -> {
                            _uiState.value = LoginUiState.RequiresTwoFactorSetup(response.challengeToken)
                        }
                        response.requiresTwoFactor == true && !response.challengeToken.isNullOrBlank() -> {
                            _uiState.value = LoginUiState.RequiresTwoFactor(response.challengeToken)
                        }
                        else -> {
                            _uiState.value = LoginUiState.Error("Respuesta de autenticación inesperada.")
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = LoginUiState.Error(error.localizedMessage ?: "Error al iniciar sesión.")
                }
            )
        }
    }
}
