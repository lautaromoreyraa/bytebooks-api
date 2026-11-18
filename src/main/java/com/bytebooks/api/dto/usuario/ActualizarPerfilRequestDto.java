package com.bytebooks.api.dto.usuario;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequestDto(
        @Size(max = 500, message = "La URL de la foto no puede superar 500 caracteres")
        @Pattern(regexp = "^$|^https://res\\.cloudinary\\.com/.+", message = "La foto tiene que ser una imagen subida al sitio")
        String fotoPerfil,

        @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
        String descripcion) { }
