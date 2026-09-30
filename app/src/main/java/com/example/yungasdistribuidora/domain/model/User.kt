package com.example.yungasdistribuidora.domain.model

enum class UserRole {
    ADMIN,
    VENDEDOR,
    COBRADOR,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): UserRole {
            return try {
                valueOf(value?.uppercase() ?: "UNKNOWN")
            } catch (e: Exception) {
                UNKNOWN
            }
        }
    }
}

enum class UserStatus {
    PENDING_EMAIL_VERIFICATION,
    PENDING_ADMIN_APPROVAL,
    ACTIVE,
    REJECTED,
    TEMPORARILY_LOCKED,
    DISABLED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): UserStatus {
            return try {
                valueOf(value?.uppercase() ?: "UNKNOWN")
            } catch (e: Exception) {
                UNKNOWN
            }
        }
    }
}

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,
    val status: UserStatus,
    val twoFactorEnabled: Boolean
)
