package com.bytebooks.api.dto.libro;

public record GoogleBookCandidateDto(
        String titulo,
        String autor,
        String descripcion,
        String isbn,
        String anioPublicacion,
        String editorial,
        String portada
) { }
