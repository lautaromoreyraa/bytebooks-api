package com.bytebooks.api.controller;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.libro.BookImportResponseDto;
import com.bytebooks.api.dto.libro.ConfirmarImportacionRequestDto;
import com.bytebooks.api.dto.libro.GoogleBookCandidateDto;
import com.bytebooks.api.service.categoria.CategoriaService;
import com.bytebooks.api.service.libro.BookImportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/libros")
public class BookImportController {

    private final BookImportService bookImportService;
    private final CategoriaService categoriaService;

    public BookImportController(BookImportService bookImportService, CategoriaService categoriaService) {
        this.bookImportService = bookImportService;
        this.categoriaService = categoriaService;
    }

    @PostMapping("/import")
    public ResponseEntity<BookImportResponseDto> importar(
            @RequestParam String query,
            @RequestParam UUID categoriaId) {

        Categoria categoria = categoriaService.getCategoriaEntityById(categoriaId);

        return ResponseEntity.ok(bookImportService.importarDesdeGoogle(query, categoria));
    }

    @GetMapping("/import/previsualizar")
    public ResponseEntity<List<GoogleBookCandidateDto>> previsualizar(@RequestParam String query) {
        return ResponseEntity.ok(bookImportService.previsualizarDesdeGoogle(query));
    }

    @PostMapping("/import/confirmar")
    public ResponseEntity<BookImportResponseDto> confirmar(@Valid @RequestBody ConfirmarImportacionRequestDto request) {
        Categoria categoria = categoriaService.getCategoriaEntityById(request.categoriaId());

        return ResponseEntity.ok(bookImportService.confirmarImportacion(request.libros(), categoria));
    }
}
