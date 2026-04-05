package com.bytebooks.api.dto.usuario;

import java.util.UUID;

public record UsuarioPerfilResponseDto(
        UUID id,
        String nombre,
        String apellido,
        String fotoPerfil,
        String descripcion) { }
