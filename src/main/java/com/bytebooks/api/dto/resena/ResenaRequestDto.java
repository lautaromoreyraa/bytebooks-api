package com.bytebooks.api.dto.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResenaRequestDto(
        @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
        String comentario,

        @NotNull(message = "La puntuacion es obligatoria")
        @Min(value = 1, message = "La puntuacion minima es 1")
        @Max(value = 5, message = "La puntuacion maxima es 5")
        Integer puntuacion) { }
