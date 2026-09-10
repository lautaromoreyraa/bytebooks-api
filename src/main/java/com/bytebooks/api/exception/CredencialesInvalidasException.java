package com.bytebooks.api.exception;

/**
 * Email o contrasena que no coinciden.
 *
 * Existe como excepcion propia en vez de reusar IllegalArgumentException porque
 * esa es demasiado generica para mapearla entera a 401: cualquier
 * UUID.fromString() con basura adentro tambien la lanza, y esa es una peticion
 * mal formada, no un intento de ingreso fallido.
 *
 * El mensaje no distingue entre "ese email no existe" y "la contrasena esta
 * mal", a proposito: la diferencia le sirve mas a quien prueba cuentas ajenas
 * que a quien se equivoco al tipear.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
