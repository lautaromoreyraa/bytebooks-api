package com.bytebooks.api.service.libro;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.libro.ImportacionDeLibrosResponseDto;
import com.bytebooks.api.dto.libro.CandidatoDeGoogleBooksDto;

import java.util.List;

public interface ImportacionDeLibrosService {
    ImportacionDeLibrosResponseDto importarDesdeGoogle(String query, Categoria categoria);
    List<CandidatoDeGoogleBooksDto> previsualizarDesdeGoogle(String query);
    ImportacionDeLibrosResponseDto confirmarImportacion(List<CandidatoDeGoogleBooksDto> libros, Categoria categoria);
}
