package com.bytebooks.api.service.libro.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.libro.LibroRequestDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.mapper.libro.LibroMapper;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.service.categoria.CategoriaService;
import com.bytebooks.api.service.libro.LibroService;
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
        return libroMapper.toResponseDto(libro);
    }

    @Override
    public List<LibroResponseDto> getAllLibros() {
        return libroRepository.findAll().stream()
                .map(libroMapper::toResponseDto)
                .toList();
    }

    @Override
    public LibroResponseDto agregarLibro(LibroRequestDto request) {
        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        Libro libro = new Libro();
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setDescripcion(request.descripcion());
        libro.setCategorias(categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    @Override
    public LibroResponseDto actualizarLibro(UUID id, LibroRequestDto request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Libro no encontrado con id: " + id));

        Set<Categoria> categorias = categoriaService.getCategoriaEntitiesByIds(request.categoriaIds());

        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setDescripcion(request.descripcion());
        libro.setCategorias(categorias);

        return libroMapper.toResponseDto(libroRepository.save(libro));
    }

    @Override
    public void eliminarLibro(UUID id) {
        if (!libroRepository.existsById(id)) {
            throw new NoSuchElementException("Libro no encontrado con id: " + id);
        }
        libroRepository.deleteById(id);
    }
}
