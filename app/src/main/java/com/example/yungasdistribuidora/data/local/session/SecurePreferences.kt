package com.example.yungasdistribuidora.data.local.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

/** Never delete unreadable credentials or fall back to storing credentials in plain text. */
internal fun openSecurePreferences(context: Context, name: String): SharedPreferences? {
    fun open(file: String, alias: String): SharedPreferences? {
        return try {
            val key = MasterKey.Builder(context, alias)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(
                context, file, key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).also { it.all } // Validate values too, before returning the store.
        } catch (_: GeneralSecurityException) {
            Log.w("SecurePreferences", "Encrypted session unavailable; authentication must be renewed")
            null
        } catch (_: IOException) {
            Log.w("SecurePreferences", "Encrypted session storage unavailable")
            null
        } catch (_: SecurityException) {
            Log.w("SecurePreferences", "Encrypted session could not be authenticated")
            null
        }
    }
    // An isolated encrypted store preserves the original file and all business data.
    // If even this fails, callers use memory only and disable offline authentication.
    return open(name, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        ?: open("${name}_recovery", "${name}_recovery_key")
}
