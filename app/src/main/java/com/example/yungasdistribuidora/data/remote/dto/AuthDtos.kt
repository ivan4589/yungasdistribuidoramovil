package com.example.yungasdistribuidora.data.remote.dto

import com.example.yungasdistribuidora.domain.model.User
import com.example.yungasdistribuidora.domain.model.UserRole
import com.example.yungasdistribuidora.domain.model.UserStatus

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val requiresTwoFactor: Boolean?,
    val requiresTwoFactorSetup: Boolean?,
    val challengeToken: String?
)

data class TwoFactorSetupRequest(
    val token: String
)

data class TwoFactorSetupResponse(
    val secret: String,
    val otpauthUrl: String
)

data class TwoFactorConfirmRequest(
    val challengeToken: String,
    val code: String,
    val remember: Boolean
)

data class TwoFactorVerifyRequest(
    val challengeToken: String,
    val code: String,
    val remember: Boolean
)

data class TwoFactorRecoveryRequest(
    val challengeToken: String,
    val recoveryCode: String,
    val remember: Boolean
)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val status: String,
    val twoFactorEnabled: Boolean
) {
    fun toDomain(): User {
        return User(
            id = id,
            name = name,
            email = email,
            role = UserRole.fromString(role),
            status = UserStatus.fromString(status),
            twoFactorEnabled = twoFactorEnabled
        )
    }
}

data class AuthResponse(
    val access_token: String,
    val user: UserDto,
    val recoveryCodes: List<String>? = null
)

data class LogoutRequest(
    val empty: Boolean = true
)
