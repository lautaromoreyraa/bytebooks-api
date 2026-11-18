package com.bytebooks.api.exception;

public class GoogleBooksNoDisponibleException extends RuntimeException {

    public GoogleBooksNoDisponibleException() {
        super("Google Books no responde en este momento. Probá de nuevo en unos minutos.");
    }
}
