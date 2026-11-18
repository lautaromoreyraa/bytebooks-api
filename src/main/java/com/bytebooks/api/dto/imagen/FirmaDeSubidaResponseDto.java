package com.bytebooks.api.dto.imagen;

public record FirmaDeSubidaResponseDto(
        String firma,
        long marcaDeTiempo,
        String apiKey,
        String nombreDeLaNube,
        String carpeta) {
}
