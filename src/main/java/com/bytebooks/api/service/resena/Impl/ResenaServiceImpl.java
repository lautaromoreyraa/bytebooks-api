package com.bytebooks.api.service.resena.Impl;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.domain.Resena;
import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.dto.resena.ResenaRequestDto;
import com.bytebooks.api.dto.resena.ResenaResponseDto;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.repository.ResenaRepository;
import com.bytebooks.api.service.resena.ResenaService;
import com.bytebooks.api.service.usuario.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final LibroRepository libroRepository;
    private final UsuarioService usuarioService;

    public ResenaServiceImpl(ResenaRepository resenaRepository,
                             LibroRepository libroRepository,
                             UsuarioService usuarioService) {
        this.resenaRepository = resenaRepository;
        this.libroRepository = libroRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public Page<ResenaResponseDto> getResenasByLibro(UUID libroId, Pageable pageable) {
        return resenaRepository.findByLibroId(libroId, pageable).map(this::toResponseDto);
    }

    @Override
    public ResenaResponseDto crearResena(UUID libroId, UUID usuarioId, ResenaRequestDto request) {
        if (resenaRepository.findByLibroIdAndUsuarioId(libroId, usuarioId).isPresent()) {
            throw new IllegalStateException("Ya existe una reseña tuya para este libro");
        }

        Usuario usuario = usuarioService.getUsuarioEntityById(usuarioId);

        Resena resena = new Resena();
        resena.setLibro(buildLibroRef(libroId));
        resena.setUsuario(usuario);
        resena.setComentario(request.comentario());
        resena.setPuntuacion(request.puntuacion());
        resena.setFechaResena(new Date());

        return toResponseDto(resenaRepository.save(resena));
    }

    @Override
    public ResenaResponseDto actualizarResena(UUID resenaId, UUID usuarioId, ResenaRequestDto request) {
        Resena resena = getResenaById(resenaId);
        verificarPermisos(resena, usuarioId);
        resena.setComentario(request.comentario());
        resena.setPuntuacion(request.puntuacion());
        return toResponseDto(resenaRepository.save(resena));
    }

    @Override
    public void eliminarResena(UUID resenaId, UUID usuarioId) {
        Resena resena = getResenaById(resenaId);
        verificarPermisos(resena, usuarioId);
        resenaRepository.deleteById(resenaId);
    }

    private Resena getResenaById(UUID id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reseña no encontrada con id: " + id));
    }

    private void verificarPermisos(Resena resena, UUID usuarioId) {
        if (!resena.getUsuario().getId().equals(usuarioId) && !puedeGestionarResenas()) {
            throw new AccessDeniedException("No tienes permiso para modificar esta reseña");
        }
    }

    private boolean puedeGestionarResenas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        RolEnum.ROLE_ADMIN.name().equals(authority.getAuthority())
                                || RolEnum.ROLE_MODERATOR.name().equals(authority.getAuthority()));
    }

    private Libro buildLibroRef(UUID libroId) {
        return libroRepository.findById(libroId)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + libroId));
    }

    private ResenaResponseDto toResponseDto(Resena r) {
        return new ResenaResponseDto(
                r.getId(),
                r.getLibro().getId(),
                r.getUsuario().getId(),
                r.getUsuario().getNombre() + " " + r.getUsuario().getApellido(),
                r.getComentario(),
                r.getPuntuacion(),
                r.getFechaResena());
    }
}
