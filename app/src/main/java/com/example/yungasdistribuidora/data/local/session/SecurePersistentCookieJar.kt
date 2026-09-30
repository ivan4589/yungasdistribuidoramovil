package com.example.yungasdistribuidora.data.local.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class SecurePersistentCookieJar(context: Context) : CookieJar {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_cookies_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()
    private val cookiesMap = ConcurrentHashMap<String, ConcurrentHashMap<String, Cookie>>()

    init {
        loadAllCookies()
    }

    private fun loadAllCookies() {
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                try {
                    val cookieJson = gson.fromJson(value, CookieWrapper::class.java)
                    val cookie = cookieJson.toCookie()
                    if (cookie != null && (cookie.expiresAt == -1L || cookie.expiresAt > System.currentTimeMillis())) {
                        cookiesMap.getOrPut(cookie.domain) { ConcurrentHashMap() }[cookie.name] = cookie
                    }
                } catch (e: Exception) {
                    // ignore corrupted entries
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val domain = url.host
        val domainCookies = cookiesMap.getOrPut(domain) { ConcurrentHashMap() }
        val editor = prefs.edit()
        
        for (cookie in cookies) {
            if (cookie.expiresAt <= System.currentTimeMillis() && cookie.expiresAt != -1L) {
                domainCookies.remove(cookie.name)
                editor.remove(getPrefsKey(domain, cookie.name))
            } else {
                domainCookies[cookie.name] = cookie
                val wrapper = CookieWrapper.fromCookie(cookie)
                editor.putString(getPrefsKey(domain, cookie.name), gson.toJson(wrapper))
            }
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val currentMillis = System.currentTimeMillis()
        val validCookies = mutableListOf<Cookie>()
        val editor = prefs.edit()
        
        cookiesMap.forEach { (domain, domainCookies) ->
            if (url.host.equals(domain, ignoreCase = true) || url.host.endsWith(".$domain")) {
                domainCookies.values.removeIf { cookie ->
                    if (cookie.expiresAt <= currentMillis && cookie.expiresAt != -1L) {
                        editor.remove(getPrefsKey(domain, cookie.name))
                        true
                    } else if (cookie.matches(url)) {
                        validCookies.add(cookie)
                        false
                    } else {
                        false
                    }
                }
            }
        }
        editor.apply()
        return validCookies
    }

    fun clear() {
        cookiesMap.clear()
        prefs.edit().clear().apply()
    }

    private fun getPrefsKey(domain: String, name: String): String {
        return "${domain}_$name"
    }

    private data class CookieWrapper(
        val name: String,
        val value: String,
        val expiresAt: Long,
        val domain: String,
        val path: String,
        val secure: Boolean,
        val httpOnly: Boolean,
        val hostOnly: Boolean
    ) {
        companion object {
            fun fromCookie(cookie: Cookie): CookieWrapper {
                return CookieWrapper(
                    name = cookie.name,
                    value = cookie.value,
                    expiresAt = cookie.expiresAt,
                    domain = cookie.domain,
                    path = cookie.path,
                    secure = cookie.secure,
                    httpOnly = cookie.httpOnly,
                    hostOnly = cookie.hostOnly
                )
            }
        }

        fun toCookie(): Cookie? {
            return try {
                val builder = Cookie.Builder()
                    .name(name)
                    .value(value)
                    .expiresAt(expiresAt)
                    .path(path)
                if (secure) builder.secure()
                if (httpOnly) builder.httpOnly()
                if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
                builder.build()
            } catch (e: Exception) {
                null
            }
        }
    }
}
