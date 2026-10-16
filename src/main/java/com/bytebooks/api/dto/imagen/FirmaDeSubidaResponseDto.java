package com.bytebooks.api.dto.imagen;

/**
 * Lo que necesita el navegador para subir una imagen directo a Cloudinary.
 * Lleva la api_key, que es publica; el secreto con el que se calculo la firma
 * no sale nunca del servidor.
 */
public record FirmaDeSubidaResponseDto(
        String firma,
        long marcaDeTiempo,
        String apiKey,
        String nombreDeLaNube,
        String carpeta) {
}
