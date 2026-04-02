package com.bytebooks.api.mapper.categoria.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.dto.categoria.CategoriaResponseDto;
import com.bytebooks.api.mapper.categoria.CategoriaMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapperImpl implements CategoriaMapper {

    @Override
    public Categoria toCategoria(CategoriaRequestDto dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());
        return categoria;
    }

    @Override
    public CategoriaResponseDto toResponseDto(Categoria categoria) {
        return new CategoriaResponseDto(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
