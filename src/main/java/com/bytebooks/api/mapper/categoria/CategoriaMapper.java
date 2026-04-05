package com.bytebooks.api.mapper.categoria;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.dto.categoria.CategoriaResponseDto;

public interface CategoriaMapper {
    Categoria toCategoria(CategoriaRequestDto categoriaRequestDto);
    CategoriaResponseDto toResponseDto(Categoria categoria);
}
