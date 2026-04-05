package com.bytebooks.api.dto.usuario;

import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequestDto(
        @Size(max = 500, message = "La URL de la foto no puede superar 500 caracteres")
        String fotoPerfil,

        @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
        String descripcion) { }
