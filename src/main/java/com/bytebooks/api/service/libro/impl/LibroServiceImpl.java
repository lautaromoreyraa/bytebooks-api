package com.bytebooks.api.service.libro.impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.libro.LibroRequestDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import com.bytebooks.api.mapper.libro.LibroMapper;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.repository.ResenaRepository;
import com.bytebooks.api.service.categoria.CategoriaService;
import com.bytebooks.api.service.libro.LibroService;
import com.bytebooks.api.service.libro.VisibilidadDeLibros;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaService categoriaService;
    private final LibroMapper libroMapper;
    private final VisibilidadDeLibros visibilidad;
    private final ResenaRepository resenaRepository;

    public LibroServiceImpl(LibroRepository libroRepository,
                            CategoriaService categoriaService,
                            LibroMapper libroMapper,
                            VisibilidadDeLibros visibilidad,
                            ResenaRepository resenaRepository) {
        this.libroRepository = libroRepository;
        this.categoriaService = categoriaService;
        this.libroMapper = libroMapper;
        this.visibilidad = visibilidad;
        this.resenaRepository = resenaRepository;
    }

    @Override
    public LibroResponseDto getLibroById(UUID id) {
        Libro libro = libroRepository.findByIdConCategorias(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + id));

        if (!visibilidad.esVisible(libro)) {
            throw new NoSuchElementException("Libro no encontrado con id: " + id);
        }

        return libroMapper.toResponseDto(libro);
    }

    @Override
    public List<LibroResponseDto> getAllLibros() {
        List<Libro> libros = visibilidad.puedeGestionar()
                ? libroRepository.findAllConCategorias()
                : libroRepository.findVisiblesConCategorias(EstadoLibroEnum.OCULTO);

        return libros.stream()
                .map(libroMapper::toResponseDto)
                .toList();
    }

    @Override
    public LibroResponseDto agregarLibro(LibroRequestDto request) {
        verificarTituloLibre(request.titulo(), null);

        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        Libro libro = new Libro();
        aplicar(libro, request, categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    @Override
    public LibroResponseDto actualizarLibro(UUID id, LibroRequestDto request) {
        Libro libro = libroRepository.findByIdConCategorias(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + id));

        verificarTituloLibre(request.titulo(), id);

        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        aplicar(libro, request, categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    private void verificarTituloLibre(String titulo, UUID idQueSeEdita) {
        boolean repetido = idQueSeEdita == null
                ? libroRepository.existsByTitulo(titulo)
                : libroRepository.existsByTituloAndIdNot(titulo, idQueSeEdita);

        if (repetido) {
            throw new IllegalStateException("Ya existe un libro con el titulo: " + titulo);
        }
    }

    private void aplicar(Libro libro, LibroRequestDto request, Set<Categoria> categorias) {
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setDescripcion(request.descripcion());
        libro.setCategorias(categorias);
        libro.setEditorial(normalizar(request.editorial()));
        libro.setAnioPublicacion(normalizar(request.anioPublicacion()));
        libro.setPortada(normalizar(request.portada()));
        libro.setEstadoLibro(request.estadoLibro() != null
                ? request.estadoLibro()
                : EstadoLibroEnum.DISPONIBLE);
    }

    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    @Override
    @Transactional
    public void eliminarLibro(UUID id) {
        if (!libroRepository.existsById(id)) {
            throw new NoSuchElementException("Libro no encontrado con id: " + id);
        }
        resenaRepository.deleteByLibroId(id);
        libroRepository.quitarDeFavoritos(id.toString());
        libroRepository.deleteById(id);
    }
}
