package com.bytebooks.api.service.usuario;

import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.dto.usuario.ActualizarPerfilRequestDto;
import com.bytebooks.api.dto.usuario.UsuarioPerfilResponseDto;

import java.util.UUID;

public interface UsuarioService {
    UsuarioPerfilResponseDto getPerfilById(UUID id);
    UsuarioPerfilResponseDto actualizarPerfil(UUID userId, ActualizarPerfilRequestDto request);
    Usuario getUsuarioEntityById(UUID id);
}
