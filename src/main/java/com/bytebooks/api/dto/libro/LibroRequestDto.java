package com.bytebooks.api.dto.libro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record LibroRequestDto(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 255, message = "El titulo no puede superar los 255 caracteres")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 100, message = "El autor no puede superar los 100 caracteres")
        String autor,

        @Size(max = 1000, message = "La descripcion no puede superar los 1000 caracteres")
        String descripcion,

        @NotEmpty(message = "Al menos una categoria es obligatoria")
        List<UUID> categoriaIds) { }
