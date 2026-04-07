package com.bytebooks.api.mapper.libro.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.categoria.CategoriaResponseDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.mapper.categoria.CategoriaMapper;
import com.bytebooks.api.mapper.libro.LibroMapper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class LibroMapperImpl implements LibroMapper {

    private final CategoriaMapper categoriaMapper;

    public LibroMapperImpl(CategoriaMapper categoriaMapper) {
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    public LibroResponseDto toResponseDto(Libro libro) {
        List<CategoriaResponseDto> categorias = libro.getCategorias().stream()
                .sorted(Comparator.comparing(Categoria::getNombre))
                .map(categoriaMapper::toResponseDto)
                .toList();

        return new LibroResponseDto(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getDescripcion(),
                categorias,
                libro.getEditorial(),
                libro.getAnioPublicacion(),
                libro.getPortada(),
                libro.getEstadoLibro()
        );
    }
}
