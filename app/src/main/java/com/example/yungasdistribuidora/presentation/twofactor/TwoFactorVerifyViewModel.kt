package com.example.yungasdistribuidora.presentation.twofactor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.TwoFactorVerifyRequest
import com.example.yungasdistribuidora.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TwoFactorVerifyUiState {
    object Idle : TwoFactorVerifyUiState
    object Loading : TwoFactorVerifyUiState
    data class Success(val role: String) : TwoFactorVerifyUiState
    data class Error(val message: String) : TwoFactorVerifyUiState
}

class TwoFactorVerifyViewModel(
    private val authRepository: AuthRepository,
    private val challengeToken: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<TwoFactorVerifyUiState>(TwoFactorVerifyUiState.Idle)
    val uiState: StateFlow<TwoFactorVerifyUiState> = _uiState.asStateFlow()

    fun verify(code: String, remember: Boolean) {
        if (code.length != 6 || !code.all { it.isDigit() }) {
            _uiState.value = TwoFactorVerifyUiState.Error("El código debe tener exactamente 6 dígitos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = TwoFactorVerifyUiState.Loading
            val result = authRepository.verifyTwoFactor(TwoFactorVerifyRequest(challengeToken, code, remember))
            result.fold(
                onSuccess = { authResponse ->
                    val role = authResponse.user.role
                    _uiState.value = TwoFactorVerifyUiState.Success(role)
                },
                onFailure = { error ->
                    _uiState.value = TwoFactorVerifyUiState.Error(error.localizedMessage ?: "Código 2FA inválido o expirado.")
                }
            )
        }
    }
}
