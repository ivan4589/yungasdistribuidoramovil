package com.example.yungasdistribuidora.data.local.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.yungasdistribuidora.domain.model.User
import com.google.gson.Gson

const val OFFLINE_SESSION_MAX_AGE_HOURS = 48L

data class OfflineSession(
    val userId: Long,
    val name: String,
    val email: String,
    val role: com.example.yungasdistribuidora.domain.model.UserRole,
    val status: com.example.yungasdistribuidora.domain.model.UserStatus,
    val lastOnlineValidationAt: Long,
    val offlineEnabled: Boolean
) {
    fun isExpired(): Boolean {
        val maxAgeMillis = OFFLINE_SESSION_MAX_AGE_HOURS * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        if (now < lastOnlineValidationAt) return true
        return (now - lastOnlineValidationAt) > maxAgeMillis
    }
}

class OfflineSessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "offline_session_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    fun saveSession(user: User, remember: Boolean) {
        val session = OfflineSession(
            userId = user.id,
            name = user.name,
            email = user.email,
            role = user.role,
            status = user.status,
            lastOnlineValidationAt = System.currentTimeMillis(),
            offlineEnabled = remember
        )
        prefs.edit().putString("offline_session", gson.toJson(session)).apply()
    }

    fun getSession(): OfflineSession? {
        val json = prefs.getString("offline_session", null) ?: return null
        return try {
            gson.fromJson(json, OfflineSession::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
