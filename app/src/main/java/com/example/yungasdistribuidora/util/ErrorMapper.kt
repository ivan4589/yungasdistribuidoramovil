package com.example.yungasdistribuidora.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object ErrorMapper {
    fun mapException(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "No tienes conexión a internet."
            is SocketTimeoutException -> "La conexión tardó demasiado. Intenta nuevamente."
            is ConnectException -> "No se pudo conectar con el servidor."
            is SSLException -> "No se pudo establecer una conexión segura."
            else -> {
                val msg = throwable.localizedMessage ?: ""
                when {
                    msg.contains("401") -> "Credenciales inválidas, código incorrecto o sesión expirada."
                    msg.contains("403") -> "Tu cuenta no tiene autorización para realizar esta acción."
                    msg.contains("429") -> "Demasiados intentos. Espera unos minutos antes de volver a intentarlo."
                    msg.contains("50") || msg.contains("51") || msg.contains("52") || msg.contains("53") || msg.contains("54") -> "El servidor no está disponible temporalmente. Intenta nuevamente."
                    else -> "No tienes conexión a internet. Conéctate para iniciar sesión por primera vez en este dispositivo."
                }
            }
        }
    }

    fun mapHttpCode(code: Int): String {
        return when (code) {
            401 -> "Credenciales inválidas, código incorrecto o sesión expirada."
            403 -> "Tu cuenta no tiene autorización para realizar esta acción."
            429 -> "Demasiados intentos. Espera unos minutos antes de volver a intentarlo."
            in 500..599 -> "El servidor no está disponible temporalmente. Intenta nuevamente."
            else -> "Ocurrió un error inesperado. Intenta nuevamente."
        }
    }
}
