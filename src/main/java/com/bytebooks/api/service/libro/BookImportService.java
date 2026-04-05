package com.bytebooks.api.service.libro;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.dto.libro.BookImportResponseDto;

public interface BookImportService {
    BookImportResponseDto importarDesdeGoogle(String query, Categoria categoria);
}
