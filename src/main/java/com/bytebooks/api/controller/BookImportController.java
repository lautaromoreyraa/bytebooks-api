package com.bytebooks.api.controller;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.libro.BookImportResponseDto;
import com.bytebooks.api.repository.CategoriaRepository;
import com.bytebooks.api.service.libro.BookImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/libros")
public class BookImportController {

    private final BookImportService bookImportService;
    private final CategoriaRepository categoriaRepository;

    public BookImportController(BookImportService bookImportService, CategoriaRepository categoriaRepository) {
        this.bookImportService = bookImportService;
        this.categoriaRepository = categoriaRepository;
    }

    @PostMapping("/import")
    public ResponseEntity<BookImportResponseDto> importar(
            @RequestParam String query,
            @RequestParam UUID categoriaId) {

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada con id: " + categoriaId));

        BookImportResponseDto resultado = bookImportService.importarDesdeGoogle(query, categoria);
        return ResponseEntity.ok(resultado);
    }
}
