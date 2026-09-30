package com.example.yungasdistribuidora.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yungasdistribuidora.data.local.session.OfflineSession
import com.example.yungasdistribuidora.data.local.session.OfflineSessionManager
import com.example.yungasdistribuidora.data.repository.AuthRepository
import com.example.yungasdistribuidora.domain.model.UserRole
import com.example.yungasdistribuidora.domain.model.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashUiState {
    object Loading : SplashUiState
    data class NavigateToOnline(val route: String) : SplashUiState
    data class NavigateToOffline(val session: OfflineSession) : SplashUiState
    data class RequireBiometric(val session: OfflineSession) : SplashUiState
    data class Error(val message: String) : SplashUiState
    object NavigateToLoginNoConnection : SplashUiState
    object NavigateToLoginExpired : SplashUiState
    object DeviceSecurityUnavailable : SplashUiState
}

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val offlineSessionManager: OfflineSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading
            val refreshResult = authRepository.refreshSession()
            if (refreshResult.isSuccess) {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    if (user != null && user.status == UserStatus.ACTIVE) {
                        val route = when (user.role) {
                            UserRole.ADMIN -> "admin_home"
                            UserRole.VENDEDOR -> "vendor_home"
                            UserRole.COBRADOR -> "role_not_available"
                            else -> "login"
                        }
                        if (route != "login") {
                            _uiState.value = SplashUiState.NavigateToOnline(route)
                        } else {
                            authRepository.clearLocalSession()
                            _uiState.value = SplashUiState.NavigateToLoginNoConnection
                        }
                    } else {
                        authRepository.clearLocalSession()
                        _uiState.value = SplashUiState.NavigateToLoginNoConnection
                    }
                } else {
                    handleOfflineFallback()
                }
            } else {
                handleOfflineFallback()
            }
        }
    }

    private fun handleOfflineFallback() {
        val offlineSession = offlineSessionManager.getSession()
        if (offlineSession != null && offlineSession.offlineEnabled && offlineSession.status == UserStatus.ACTIVE) {
            if (offlineSession.isExpired()) {
                _uiState.value = SplashUiState.NavigateToLoginExpired
            } else {
                _uiState.value = SplashUiState.RequireBiometric(offlineSession)
            }
        } else {
            _uiState.value = SplashUiState.NavigateToLoginNoConnection
        }
    }

    fun onBiometricSuccess(session: OfflineSession) {
        _uiState.value = SplashUiState.NavigateToOffline(session)
    }

    fun onBiometricFailed() {
        _uiState.value = SplashUiState.Error("Autenticación biométrica cancelada o fallida.")
    }
}
