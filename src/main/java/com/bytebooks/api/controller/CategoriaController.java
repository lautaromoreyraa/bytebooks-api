package com.bytebooks.api.controller;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.service.categoria.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping
    public Categoria agregarCategoria(@RequestBody CategoriaRequestDto categoriaDto) {
        return categoriaService.agregarCategoria( categoriaDto );
    }

    @GetMapping("/{id}")
    public Categoria getCategoriaById(@PathVariable("id") UUID id) {
        return categoriaService.getCategoriaById( id );
    }

    @GetMapping
    public java.util.List<Categoria> getAllCategorias() {
        return categoriaService.getAllCategorias();
    }
}
