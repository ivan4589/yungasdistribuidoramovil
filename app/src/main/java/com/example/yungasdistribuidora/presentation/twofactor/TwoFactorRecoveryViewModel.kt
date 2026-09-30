package com.example.yungasdistribuidora.presentation.twofactor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.TwoFactorRecoveryRequest
import com.example.yungasdistribuidora.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TwoFactorRecoveryUiState {
    object Idle : TwoFactorRecoveryUiState
    object Loading : TwoFactorRecoveryUiState
    data class Success(val role: String) : TwoFactorRecoveryUiState
    data class Error(val message: String) : TwoFactorRecoveryUiState
}

class TwoFactorRecoveryViewModel(
    private val authRepository: AuthRepository,
    private val challengeToken: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<TwoFactorRecoveryUiState>(TwoFactorRecoveryUiState.Idle)
    val uiState: StateFlow<TwoFactorRecoveryUiState> = _uiState.asStateFlow()

    fun recover(recoveryCode: String, remember: Boolean) {
        val code = recoveryCode.trim()
        if (code.isBlank()) {
            _uiState.value = TwoFactorRecoveryUiState.Error("El código de recuperación no puede estar vacío.")
            return
        }

        viewModelScope.launch {
            _uiState.value = TwoFactorRecoveryUiState.Loading
            val result = authRepository.recoverTwoFactor(TwoFactorRecoveryRequest(challengeToken, code, remember))
            result.fold(
                onSuccess = { authResponse ->
                    val role = authResponse.user.role
                    _uiState.value = TwoFactorRecoveryUiState.Success(role)
                },
                onFailure = { error ->
                    _uiState.value = TwoFactorRecoveryUiState.Error(error.localizedMessage ?: "Código de recuperación inválido o ya utilizado.")
                }
            )
        }
    }
}
