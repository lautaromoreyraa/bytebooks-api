package com.bytebooks.api.mapper.categoria.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.mapper.categoria.CategoriaMapper;
import com.bytebooks.api.service.categoria.CategoriaService;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapperImpl implements CategoriaMapper {

    @Override
    public Categoria CategoriaRequestDtoToCategoria(CategoriaRequestDto categoriaRequestDto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaRequestDto.nombre());
        categoria.setDescripcion(categoriaRequestDto.descripcion());
        return categoria;
    }

    @Override
    public CategoriaRequestDto CategoriaDtoToCategoriaResponseDto(Categoria categoria) {
        return new CategoriaRequestDto(
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}
