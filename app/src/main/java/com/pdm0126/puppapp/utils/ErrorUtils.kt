package com.pdm0126.puppapp.utils

import io.ktor.client.plugins.ResponseException
import java.io.IOException

fun Throwable.toUserFriendlyMessage(isLogin: Boolean = false, isRegister: Boolean = false): String {
    return when (this) {
        is ResponseException -> {
            when (this.response.status.value) {
                400 -> "Los datos enviados son incorrectos. Por favor, revísalos."
                401 -> if (isLogin) "Usuario o contraseña incorrectos." else "Tu sesión ha expirado. Vuelve a iniciar sesión."
                403 -> "No tienes permiso para realizar esta acción."
                404 -> "Lo que buscas no se ha encontrado en el servidor."
                409 -> if (isRegister) "Ese nombre de usuario ya está ocupado." else "Hubo un conflicto con los datos."
                500, 502, 503, 504 -> "El servidor está teniendo problemas en este momento. Inténtalo más tarde."
                else -> "Ha ocurrido un error en la solicitud (${this.response.status.value})."
            }
        }
        is IOException -> {
            "No se pudo conectar al servidor. Revisa tu conexión a internet."
        }
        else -> {
            this.localizedMessage ?: "Ha ocurrido un error inesperado. Inténtalo de nuevo."
        }
    }
}
