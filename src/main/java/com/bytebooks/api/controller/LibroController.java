package com.bytebooks.api.controller;

import com.bytebooks.api.dto.libro.LibroRequestDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.service.libro.LibroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @PostMapping
    public ResponseEntity<LibroResponseDto> agregarLibro(@Valid @RequestBody LibroRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.agregarLibro(request));
    }

    @GetMapping
    public ResponseEntity<List<LibroResponseDto>> getAllLibros() {
        return ResponseEntity.ok(libroService.getAllLibros());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroResponseDto> getLibroById(@PathVariable UUID id) {
        return ResponseEntity.ok(libroService.getLibroById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibroResponseDto> actualizarLibro(@PathVariable UUID id,
                                                            @Valid @RequestBody LibroRequestDto request) {
        return ResponseEntity.ok(libroService.actualizarLibro(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable UUID id) {
        libroService.eliminarLibro(id);
        return ResponseEntity.noContent().build();
    }
}
