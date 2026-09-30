package com.example.yungasdistribuidora.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException

class ErrorMapperTest {

    @Test
    fun testUnknownHostExceptionMappedToSpanish() {
        val ex = UnknownHostException("Unable to resolve host \"api.yungasdistribuidora.cc\"")
        val message = ErrorMapper.mapException(ex)
        assertEquals("No tienes conexión a internet.", message)
    }

    @Test
    fun testHttp401Mapped() {
        val message = ErrorMapper.mapHttpCode(401)
        assertEquals("Credenciales inválidas, código incorrecto o sesión expirada.", message)
    }

    @Test
    fun testHttp500Mapped() {
        val message = ErrorMapper.mapHttpCode(500)
        assertEquals("El servidor no está disponible temporalmente. Intenta nuevamente.", message)
    }
}
