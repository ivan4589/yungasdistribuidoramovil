package com.example.yungasdistribuidora.presentation.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(
    private val appLockManager: AppLockManager
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        appLockManager.onAppStopped()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        appLockManager.onAppStarted()
    }
}
