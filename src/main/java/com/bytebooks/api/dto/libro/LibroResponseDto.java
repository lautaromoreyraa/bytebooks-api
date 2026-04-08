package com.bytebooks.api.dto.libro;

import com.bytebooks.api.dto.categoria.CategoriaResponseDto;
import com.bytebooks.api.enumeration.EstadoLibroEnum;

import java.util.List;
import java.util.UUID;

public record LibroResponseDto(
        UUID id,
        String titulo,
        String autor,
        String descripcion,
        List<CategoriaResponseDto> categorias,
        String editorial,
        String anioPublicacion,
        String portada,
        EstadoLibroEnum estadoLibro) { }
