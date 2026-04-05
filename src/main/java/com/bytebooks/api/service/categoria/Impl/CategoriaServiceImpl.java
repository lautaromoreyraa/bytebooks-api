package com.bytebooks.api.service.categoria.Impl;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.dto.categoria.CategoriaResponseDto;
import com.bytebooks.api.mapper.categoria.CategoriaMapper;
import com.bytebooks.api.repository.CategoriaRepository;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.service.categoria.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository,
                                LibroRepository libroRepository,
                                CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.libroRepository = libroRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    public CategoriaResponseDto agregarCategoria(CategoriaRequestDto request) {
        Categoria categoria = categoriaMapper.toCategoria(request);
        return categoriaMapper.toResponseDto(categoriaRepository.save(categoria));
    }

    @Override
    public CategoriaResponseDto getCategoriaById(UUID id) {
        return categoriaMapper.toResponseDto(getCategoriaEntityById(id));
    }

    @Override
    public Categoria getCategoriaEntityById(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada con id: " + id));
    }

    @Override
    public List<CategoriaResponseDto> getAllCategorias() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toResponseDto)
                .toList();
    }

    @Override
    public CategoriaResponseDto actualizarCategoria(UUID id, CategoriaRequestDto request) {
        Categoria categoria = getCategoriaEntityById(id);
        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());
        return categoriaMapper.toResponseDto(categoriaRepository.save(categoria));
    }

    @Override
    public void verifyCategoriaHasNoBooks(Categoria categoria) {
        if (libroRepository.existsByCategoria(categoria)) {
            throw new IllegalStateException(
                    "No se puede eliminar la categoria '" + categoria.getNombre() +
                    "' porque tiene libros asociados.");
        }
    }

    @Override
    public void eliminarCategoria(UUID id) {
        Categoria categoria = getCategoriaEntityById(id);
        verifyCategoriaHasNoBooks(categoria);
        categoriaRepository.deleteById(id);
    }
}
