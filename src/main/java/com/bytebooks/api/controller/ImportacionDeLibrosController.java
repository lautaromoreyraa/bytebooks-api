package com.bytebooks.api.controller;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.libro.ImportacionDeLibrosResponseDto;
import com.bytebooks.api.dto.libro.ConfirmarImportacionRequestDto;
import com.bytebooks.api.dto.libro.CandidatoDeGoogleBooksDto;
import com.bytebooks.api.service.categoria.CategoriaService;
import com.bytebooks.api.service.libro.ImportacionDeLibrosService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/libros")
public class ImportacionDeLibrosController {

    private final ImportacionDeLibrosService bookImportService;
    private final CategoriaService categoriaService;

    public ImportacionDeLibrosController(ImportacionDeLibrosService bookImportService, CategoriaService categoriaService) {
        this.bookImportService = bookImportService;
        this.categoriaService = categoriaService;
    }

    @PostMapping("/import")
    public ResponseEntity<ImportacionDeLibrosResponseDto> importar(
            @RequestParam String query,
            @RequestParam UUID categoriaId) {

        Categoria categoria = categoriaService.getCategoriaEntityById(categoriaId);

        return ResponseEntity.ok(bookImportService.importarDesdeGoogle(query, categoria));
    }

    @GetMapping("/import/previsualizar")
    public ResponseEntity<List<CandidatoDeGoogleBooksDto>> previsualizar(@RequestParam String query) {
        return ResponseEntity.ok(bookImportService.previsualizarDesdeGoogle(query));
    }

    @PostMapping("/import/confirmar")
    public ResponseEntity<ImportacionDeLibrosResponseDto> confirmar(@Valid @RequestBody ConfirmarImportacionRequestDto request) {
        Categoria categoria = categoriaService.getCategoriaEntityById(request.categoriaId());

        return ResponseEntity.ok(bookImportService.confirmarImportacion(request.libros(), categoria));
    }
}
