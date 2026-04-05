package com.bytebooks.api.dto.resena;

import java.util.Date;
import java.util.UUID;

public record ResenaResponseDto(
        UUID id,
        UUID libroId,
        UUID usuarioId,
        String nombreUsuario,
        String comentario,
        int puntuacion,
        Date fechaResena) { }
