package com.bytebooks.api.service.categoria;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;

import java.util.*;

public interface CategoriaService {
    Categoria agregarCategoria(CategoriaRequestDto categoria);
    Categoria getCategoriaById(UUID id);
    List<Categoria> getAllCategorias();
    Categoria actualizarCategoria(Categoria categoria);
    void eliminarCategoria(UUID id);
}
