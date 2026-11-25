package com.example.yungasdistribuidora.util

import com.google.gson.JsonParseException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.logging.Logger
import javax.net.ssl.SSLException

object ErrorMapper {
    fun mapException(throwable: Throwable): String {
        // Never expose SQL, row values, or Room schema dumps to the UI.
        val databaseFailure = generateSequence(throwable) { it.cause }.take(12).firstOrNull {
            val message = it.message.orEmpty()
            it.javaClass.name.startsWith("android.database.") ||
                message.contains("SQLITE_", ignoreCase = true) ||
                message.contains("Room cannot verify") ||
                (message.contains("migration from", ignoreCase = true) &&
                    message.contains("required", ignoreCase = true)) ||
                message.contains("Migration didn't properly handle") ||
                message.contains("UNIQUE constraint failed")
        }
        if (databaseFailure != null) {
            Logger.getLogger("Yungas.LocalDatabase").warning("local_database_failure: ${databaseFailure.javaClass.simpleName}")
            return "No se pudo acceder a los datos locales. Contacta al administrador."
        }
        return when (throwable) {
            is UnknownHostException, is ConnectException -> "No tienes conexión a internet."
            is SocketTimeoutException -> "La conexión tardó demasiado. Intenta nuevamente."
            is SSLException -> "No se pudo establecer una conexión segura."
            is JsonParseException -> "Error al procesar la respuesta del servidor."
            else -> {
                val msg = throwable.localizedMessage ?: ""
                when {
                    msg.contains("401") -> "Credenciales inválidas, código incorrecto o sesión expirada."
                    msg.contains("403") -> "Tu cuenta no tiene autorización para realizar esta acción."
                    msg.contains("429") -> "Demasiados intentos. Espera unos minutos antes de volver a intentarlo."
                    msg.contains("50") || msg.contains("51") || msg.contains("52") || msg.contains("53") || msg.contains("54") -> "El servidor no está disponible temporalmente. Intenta nuevamente."
                    else -> msg.ifBlank { "Ocurrió un error inesperado. Intenta nuevamente." }
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
            else -> "Ocurrió un error inesperado (código $code). Intenta nuevamente."
        }
    }
}
