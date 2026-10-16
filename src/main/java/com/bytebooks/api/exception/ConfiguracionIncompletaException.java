package com.bytebooks.api.exception;

/**
 * Falta configuracion del servidor para una funcion puntual. Responde 503 en
 * lugar de 500: el resto de la API anda, y el mensaje dice que falta.
 */
public class ConfiguracionIncompletaException extends RuntimeException {

    public ConfiguracionIncompletaException(String mensaje) {
        super(mensaje);
    }
}
