package com.bytebooks.api.service.favorito.Impl;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.mapper.libro.LibroMapper;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.repository.UsuarioRepository;
import com.bytebooks.api.service.favorito.LibroFavoritoService;
import com.bytebooks.api.service.libro.VisibilidadDeLibros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class LibroFavoritoServiceImpl implements LibroFavoritoService {

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final LibroMapper libroMapper;
    private final VisibilidadDeLibros visibilidad;

    public LibroFavoritoServiceImpl(UsuarioRepository usuarioRepository,
                                    LibroRepository libroRepository,
                                    LibroMapper libroMapper,
                                    VisibilidadDeLibros visibilidad) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository   = libroRepository;
        this.libroMapper       = libroMapper;
        this.visibilidad       = visibilidad;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibroResponseDto> getMisFavoritos(UUID usuarioId) {
        return getFavoritosDeUsuario(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibroResponseDto> getFavoritosDeUsuario(UUID usuarioId) {
        // El perfil es publico y el usuario puede no existir: eso responde 404,
        // no una lista vacia.
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new NoSuchElementException("Usuario no encontrado");
        }

        // Sin este filtro, un libro oculto que alguien tenga guardado se publica
        // entero a cualquier visitante.
        return libroRepository.findGuardadosPorUsuario(usuarioId).stream()
                .filter(visibilidad::esVisible)
                .map(libroMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void agregarFavorito(UUID usuarioId, UUID libroId) {
        Usuario usuario = getUsuario(usuarioId);
        if (usuario.getLibrosGuardados().stream().anyMatch(l -> l.getId().equals(libroId))) {
            throw new IllegalStateException("El libro ya está en tus favoritos");
        }
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado"));

        // Un libro oculto no existe para quien no puede gestionarlo, y guardarlo
        // en favoritos es otra forma de alcanzarlo: la ficha ya responde 404.
        if (!visibilidad.esVisible(libro)) {
            throw new NoSuchElementException("Libro no encontrado");
        }

        usuario.getLibrosGuardados().add(libro);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void quitarFavorito(UUID usuarioId, UUID libroId) {
        Usuario usuario = getUsuario(usuarioId);
        boolean removed = usuario.getLibrosGuardados().removeIf(l -> l.getId().equals(libroId));
        if (!removed) {
            throw new NoSuchElementException("El libro no está en tus favoritos");
        }
        usuarioRepository.save(usuario);
    }

    private Usuario getUsuario(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
