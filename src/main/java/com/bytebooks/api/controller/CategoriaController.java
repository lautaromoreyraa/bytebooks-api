package com.bytebooks.api.controller;

import com.bytebooks.api.dto.categoria.CategoriaRequestDto;
import com.bytebooks.api.dto.categoria.CategoriaResponseDto;
import com.bytebooks.api.service.categoria.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDto> agregarCategoria(@Valid @RequestBody CategoriaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.agregarCategoria(request));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDto>> getAllCategorias() {
        return ResponseEntity.ok(categoriaService.getAllCategorias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDto> getCategoriaById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.getCategoriaById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDto> actualizarCategoria(@PathVariable UUID id,
                                                                     @Valid @RequestBody CategoriaRequestDto request) {
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable UUID id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
