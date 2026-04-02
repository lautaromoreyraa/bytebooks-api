package com.bytebooks.api.dto.libro;

import com.bytebooks.api.dto.categoria.CategoriaResponseDto;
import com.bytebooks.api.enumeration.EstadoLibroEnum;

import java.util.Date;
import java.util.UUID;

public record LibroResponseDto(
        UUID id,
        String titulo,
        String autor,
        String descripcion,
        CategoriaResponseDto categoria,
        String editorial,
        Date anioPublicacion,
        EstadoLibroEnum estadoLibro) { }
