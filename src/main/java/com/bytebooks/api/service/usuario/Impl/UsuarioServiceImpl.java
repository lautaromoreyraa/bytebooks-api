package com.bytebooks.api.service.usuario.Impl;

import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.dto.usuario.ActualizarPerfilRequestDto;
import com.bytebooks.api.dto.usuario.UsuarioPerfilResponseDto;
import com.bytebooks.api.repository.UsuarioRepository;
import com.bytebooks.api.service.usuario.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioPerfilResponseDto getPerfilById(UUID id) {
        return toResponseDto(getUsuarioEntityById(id));
    }

    @Override
    public UsuarioPerfilResponseDto actualizarPerfil(UUID userId, ActualizarPerfilRequestDto request) {
        Usuario usuario = getUsuarioEntityById(userId);
        usuario.setFotoPerfil(request.fotoPerfil());
        usuario.setDescripcion(request.descripcion());
        return toResponseDto(usuarioRepository.save(usuario));
    }

    @Override
    public Usuario getUsuarioEntityById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));
    }

    private UsuarioPerfilResponseDto toResponseDto(Usuario u) {
        return new UsuarioPerfilResponseDto(
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getFotoPerfil(),
                u.getDescripcion());
    }
}
