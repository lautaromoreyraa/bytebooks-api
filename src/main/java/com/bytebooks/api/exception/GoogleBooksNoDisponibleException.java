package com.bytebooks.api.exception;

/**
 * Google Books no contestó, ni siquiera después de los reintentos. Responde 503
 * con un mensaje para el usuario: devolver una lista vacía hacía que la pantalla
 * dijera «no se encontraron resultados» cuando en realidad no se buscó nada.
 */
public class GoogleBooksNoDisponibleException extends RuntimeException {

    public GoogleBooksNoDisponibleException() {
        super("Google Books no responde en este momento. Probá de nuevo en unos minutos.");
    }
}
