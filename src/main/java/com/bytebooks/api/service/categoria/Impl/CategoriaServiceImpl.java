package com.bytebooks.api.service.categoria.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.mapper.categoria.CategoriaMapper;
import com.bytebooks.api.mapper.categoria.Impl.CategoriaMapperImpl;
import com.bytebooks.api.repository.CategoriaRepository;
import com.bytebooks.api.service.categoria.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private CategoriaMapper categoriaMapper;

    public Categoria agregarCategoria(CategoriaRequestDto request) {

        Categoria categoriaCreated = categoriaMapper.CategoriaRequestDtoToCategoria( request );

        return categoriaRepository.save( categoriaCreated );
    }

    @Override
    public Categoria getCategoriaById(UUID id) {
        return categoriaRepository.getReferenceById(id);
    }

    @Override
    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria actualizarCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    public void eliminarCategoria(UUID id) {
        categoriaRepository.deleteById(id);
    }
}
