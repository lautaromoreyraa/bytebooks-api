package com.bytebooks.api.mapper.libro.Impl;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.mapper.categoria.CategoriaMapper;
import com.bytebooks.api.mapper.libro.LibroMapper;
import org.springframework.stereotype.Component;

@Component
public class LibroMapperImpl implements LibroMapper {

    private final CategoriaMapper categoriaMapper;

    public LibroMapperImpl(CategoriaMapper categoriaMapper) {
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    public LibroResponseDto toResponseDto(Libro libro) {
        return new LibroResponseDto(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getDescripcion(),
                categoriaMapper.toResponseDto(libro.getCategoria()),
                libro.getEditorial(),
                libro.getAnioPublicacion(),
                libro.getPortada(),
                libro.getEstadoLibro()
        );
    }
}
