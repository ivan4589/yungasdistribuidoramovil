package com.example.yungasdistribuidora.presentation.twofactor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.remote.dto.TwoFactorConfirmRequest
import com.example.yungasdistribuidora.data.remote.dto.TwoFactorSetupRequest
import com.example.yungasdistribuidora.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TwoFactorSetupUiState {
    object Loading : TwoFactorSetupUiState
    data class SetupReady(val secret: String, val otpauthUrl: String) : TwoFactorSetupUiState
    data class Confirmed(val recoveryCodes: List<String>, val role: String) : TwoFactorSetupUiState
    data class Error(val message: String) : TwoFactorSetupUiState
}

class TwoFactorSetupViewModel(
    private val authRepository: AuthRepository,
    private val challengeToken: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<TwoFactorSetupUiState>(TwoFactorSetupUiState.Loading)
    val uiState: StateFlow<TwoFactorSetupUiState> = _uiState.asStateFlow()

    init {
        fetchSetupData()
    }

    fun fetchSetupData() {
        viewModelScope.launch {
            _uiState.value = TwoFactorSetupUiState.Loading
            val result = authRepository.setupTwoFactor(TwoFactorSetupRequest(challengeToken))
            result.fold(
                onSuccess = { response ->
                    _uiState.value = TwoFactorSetupUiState.SetupReady(response.secret, response.otpauthUrl)
                },
                onFailure = { error ->
                    _uiState.value = TwoFactorSetupUiState.Error(error.localizedMessage ?: "Error al obtener configuración 2FA.")
                }
            )
        }
    }

    fun confirmSetup(code: String, remember: Boolean) {
        if (code.length != 6 || !code.all { it.isDigit() }) {
            _uiState.value = TwoFactorSetupUiState.Error("El código debe tener exactamente 6 dígitos.")
            return
        }

        viewModelScope.launch {
            _uiState.value = TwoFactorSetupUiState.Loading
            val result = authRepository.confirmTwoFactor(TwoFactorConfirmRequest(challengeToken, code, remember))
            result.fold(
                onSuccess = { authResponse ->
                    val codes = authResponse.recoveryCodes ?: emptyList()
                    val role = authResponse.user.role
                    _uiState.value = TwoFactorSetupUiState.Confirmed(codes, role)
                },
                onFailure = { error ->
                    _uiState.value = TwoFactorSetupUiState.Error(error.localizedMessage ?: "Código inválido.")
                }
            )
        }
    }
}
