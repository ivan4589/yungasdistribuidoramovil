package com.example.yungasdistribuidora.presentation.lock

import com.example.yungasdistribuidora.data.local.session.OfflineSessionManager
import com.example.yungasdistribuidora.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

const val APP_LOCK_TIMEOUT_MS = 0L

class AppLockManager(
    private val authRepository: AuthRepository,
    private val offlineSessionManager: OfflineSessionManager
) {
    private val _lockState = MutableStateFlow<AppLockState>(AppLockState.NotRequired)
    val lockState = _lockState.asStateFlow()

    private var backgroundTimestamp: Long = 0L
    var isBiometricPromptActive: Boolean = false
    var lockGeneration: Long = 0L
        private set

    fun onAppStopped() {
        val hasSession = authRepository.accessToken != null || offlineSessionManager.getSession() != null
        if (hasSession) {
            backgroundTimestamp = System.currentTimeMillis()
            if (!isBiometricPromptActive && _lockState.value !is AppLockState.Authenticating) {
                _lockState.value = AppLockState.Locked
                lockGeneration++
            }
        }
    }

    fun onAppStarted() {
        val hasSession = authRepository.accessToken != null || offlineSessionManager.getSession() != null
        if (!hasSession) {
            _lockState.value = AppLockState.NotRequired
        }
    }

    fun requireUnlockAfterSessionRestore() {
        val hasSession = authRepository.accessToken != null || offlineSessionManager.getSession() != null
        if (hasSession) {
            _lockState.value = AppLockState.Locked
            lockGeneration++
            isBiometricPromptActive = false
        }
    }

    fun onInteractiveAuthenticationCompleted() {
        _lockState.value = AppLockState.Unlocked
        isBiometricPromptActive = false
    }

    fun setUnlocked() {
        _lockState.value = AppLockState.Unlocked
        isBiometricPromptActive = false
    }

    fun setLocked() {
        _lockState.value = AppLockState.Locked
        lockGeneration++
        isBiometricPromptActive = false
    }

    fun setAuthenticating(authenticating: Boolean) {
        _lockState.value = if (authenticating) AppLockState.Authenticating else AppLockState.Locked
        isBiometricPromptActive = authenticating
    }

    fun setError(message: String) {
        _lockState.value = AppLockState.Error(message)
        isBiometricPromptActive = false
    }

    fun clear() {
        _lockState.value = AppLockState.NotRequired
        backgroundTimestamp = 0L
        isBiometricPromptActive = false
        lockGeneration = 0L
    }
}
