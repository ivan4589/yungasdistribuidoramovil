package com.example.yungasdistribuidora.presentation.lock

import com.example.yungasdistribuidora.data.local.session.OfflineSessionManager
import com.example.yungasdistribuidora.data.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AppLockManagerTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val offlineSessionManager: OfflineSessionManager = mockk(relaxed = true)
    private lateinit var appLockManager: AppLockManager

    @Before
    fun setUp() {
        appLockManager = AppLockManager(authRepository, offlineSessionManager)
    }

    @Test
    fun testOnAppStoppedWithSessionTriggersLock() {
        every { authRepository.accessToken } returns "some-token"
        every { offlineSessionManager.getSession() } returns null

        val genBefore = appLockManager.lockGeneration
        appLockManager.onAppStopped()
        assertTrue(appLockManager.lockState.value is AppLockState.Locked)
        assertEquals(genBefore + 1, appLockManager.lockGeneration)
    }

    @Test
    fun testOnAppStoppedWithoutSessionDoesNotTriggerLock() {
        every { authRepository.accessToken } returns null
        every { offlineSessionManager.getSession() } returns null

        appLockManager.onAppStopped()
        assertTrue(appLockManager.lockState.value is AppLockState.NotRequired)
    }

    @Test
    fun testRequireUnlockAfterSessionRestoreSetsLocked() {
        every { authRepository.accessToken } returns "token"
        appLockManager.requireUnlockAfterSessionRestore()
        assertTrue(appLockManager.lockState.value is AppLockState.Locked)
    }

    @Test
    fun testOnInteractiveAuthenticationCompletedSetsUnlocked() {
        appLockManager.onInteractiveAuthenticationCompleted()
        assertTrue(appLockManager.lockState.value is AppLockState.Unlocked)
    }

    @Test
    fun testSetUnlockedSetsUnlockedState() {
        appLockManager.setUnlocked()
        assertTrue(appLockManager.lockState.value is AppLockState.Unlocked)
    }

    @Test
    fun testClearResetsToNotRequired() {
        appLockManager.setLocked()
        appLockManager.clear()
        assertTrue(appLockManager.lockState.value is AppLockState.NotRequired)
        assertEquals(0L, appLockManager.lockGeneration)
    }
}
