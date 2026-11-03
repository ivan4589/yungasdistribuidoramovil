package com.example.yungasdistribuidora.data.local.session

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class SecurePreferencesTest {
    @Test fun unreadableCredentialsArePreservedAndRecoveryStaysEncrypted() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "secure-test-${System.nanoTime()}"
        val key = "__androidx_security_crypto_encrypted_prefs_key_keyset__"
        try {
            // Only corrupt an isolated test file, never the application's session.
            val original = context.getSharedPreferences(name, Context.MODE_PRIVATE)
            assertTrue(original.edit().putString(key, "00").commit())
            val recovered = openSecurePreferences(context, name)
            assertNotNull(recovered)
            assertTrue(recovered!!.edit().putString("token", "test-secret").commit())
            assertEquals("00", original.getString(key, null))
            assertEquals("test-secret", openSecurePreferences(context, name)!!.getString("token", null))
            val raw = context.getSharedPreferences("${name}_recovery", Context.MODE_PRIVATE)
            assertFalse(raw.contains("token"))
            assertFalse(raw.all.values.contains("test-secret"))
        } finally {
            context.deleteSharedPreferences(name)
            context.deleteSharedPreferences("${name}_recovery")
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                .deleteEntry("${name}_recovery_key")
        }
    }
}
