package com.example.yungasdistribuidora.domain.model

enum class ClientType {
    NORMAL,
    ESPECIAL,
    CAMINO;

    companion object {
        fun fromString(value: String?): ClientType {
            return try {
                valueOf(value?.uppercase() ?: "NORMAL")
            } catch (e: Exception) {
                NORMAL
            }
        }
    }
}
