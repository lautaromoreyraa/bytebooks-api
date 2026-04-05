package com.bytebooks.api.dto.categoria;

import java.util.UUID;

public record CategoriaResponseDto(
        UUID id,
        String nombre,
        String descripcion) { }
