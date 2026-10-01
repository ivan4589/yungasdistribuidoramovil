package com.example.yungasdistribuidora.presentation.lock

sealed interface AppLockState {
    data object NotRequired : AppLockState
    data object Unlocked : AppLockState
    data object Locked : AppLockState
    data object Authenticating : AppLockState
    data class Error(val message: String) : AppLockState
}
