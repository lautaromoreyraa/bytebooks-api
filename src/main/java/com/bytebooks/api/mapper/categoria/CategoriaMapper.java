package com.bytebooks.api.mapper.categoria;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;

public interface CategoriaMapper {
    Categoria CategoriaRequestDtoToCategoria (CategoriaRequestDto categoriaRequestDto);
    CategoriaRequestDto CategoriaDtoToCategoriaResponseDto (Categoria categoria);
}
