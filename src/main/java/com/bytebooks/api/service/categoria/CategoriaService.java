package com.bytebooks.api.service.categoria;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.dto.categoria.CategoriaResponseDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CategoriaService {
    CategoriaResponseDto agregarCategoria(CategoriaRequestDto request);
    CategoriaResponseDto getCategoriaById(UUID id);
    Categoria getCategoriaEntityById(UUID id);
    Set<Categoria> getCategoriaEntitiesByIds(List<UUID> ids);
    List<CategoriaResponseDto> getAllCategorias();
    CategoriaResponseDto actualizarCategoria(UUID id, CategoriaRequestDto request);
    void eliminarCategoria(UUID id);
    void verifyCategoriaHasNoBooks(Categoria categoria);
}
