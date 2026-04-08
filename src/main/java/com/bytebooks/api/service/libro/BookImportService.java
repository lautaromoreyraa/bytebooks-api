package com.bytebooks.api.service.libro;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.libro.BookImportResponseDto;
import com.bytebooks.api.dto.libro.GoogleBookCandidateDto;

import java.util.List;

public interface BookImportService {
    BookImportResponseDto importarDesdeGoogle(String query, Categoria categoria);
    List<GoogleBookCandidateDto> previsualizarDesdeGoogle(String query);
    BookImportResponseDto confirmarImportacion(List<GoogleBookCandidateDto> libros, Categoria categoria);
}
