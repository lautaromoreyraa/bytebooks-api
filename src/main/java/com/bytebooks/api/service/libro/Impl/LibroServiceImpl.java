package com.bytebooks.api.service.libro.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.libro.LibroRequestDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.mapper.libro.LibroMapper;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.service.categoria.CategoriaService;
import com.bytebooks.api.service.libro.LibroService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaService categoriaService;
    private final LibroMapper libroMapper;

    public LibroServiceImpl(LibroRepository libroRepository,
                            CategoriaService categoriaService,
                            LibroMapper libroMapper) {
        this.libroRepository = libroRepository;
        this.categoriaService = categoriaService;
        this.libroMapper = libroMapper;
    }

    @Override
    public LibroResponseDto getLibroById(UUID id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + id));

        if (!esVisiblePara(libro)) {
            throw new NoSuchElementException("Libro no encontrado con id: " + id);
        }

        return libroMapper.toResponseDto(libro);
    }

    @Override
    public List<LibroResponseDto> getAllLibros() {
        return libroRepository.findAll().stream()
                .filter(this::esVisiblePara)
                .map(libroMapper::toResponseDto)
                .toList();
    }

    /**
     * Un libro OCULTO sólo lo ven quienes pueden gestionarlo: si no, el estado
     * no ocultaba nada y el catálogo público los listaba igual.
     * Para el resto se comporta como si no existiera, en lugar de responder 403,
     * para no confirmar que el id corresponde a un libro real.
     */
    private boolean esVisiblePara(Libro libro) {
        return libro.getEstadoLibro() != EstadoLibroEnum.OCULTO || puedeGestionarLibros();
    }

    private boolean puedeGestionarLibros() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        RolEnum.ROLE_ADMIN.name().equals(authority.getAuthority())
                                || RolEnum.ROLE_MODERATOR.name().equals(authority.getAuthority()));
    }

    @Override
    public LibroResponseDto agregarLibro(LibroRequestDto request) {
        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        Libro libro = new Libro();
        aplicar(libro, request, categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    @Override
    public LibroResponseDto actualizarLibro(UUID id, LibroRequestDto request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + id));

        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        aplicar(libro, request, categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    /**
     * Editorial, anio, portada y estado llegaban desde el formulario pero no se
     * persistian: el DTO no los declaraba y Jackson los descartaba en silencio.
     * PUT reemplaza el recurso completo, asi que un cliente que omita un campo
     * lo deja vacio; el unico consumidor los envia siempre.
     */
    private void aplicar(Libro libro, LibroRequestDto request, Set<Categoria> categorias) {
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setDescripcion(request.descripcion());
        libro.setCategorias(categorias);
        libro.setEditorial(request.editorial());
        libro.setAnioPublicacion(normalizar(request.anioPublicacion()));
        libro.setPortada(request.portada());
        libro.setEstadoLibro(request.estadoLibro() != null
                ? request.estadoLibro()
                : EstadoLibroEnum.DISPONIBLE);
    }

    /** La columna admite 4 caracteres: un string vacio se guarda como null. */
    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    @Override
    public void eliminarLibro(UUID id) {
        if (!libroRepository.existsById(id)) {
            throw new NoSuchElementException("Libro no encontrado con id: " + id);
        }
        libroRepository.deleteById(id);
    }
}
