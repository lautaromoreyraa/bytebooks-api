package com.bytebooks.api.mapper.libro;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.dto.libro.LibroResponseDto;

public interface LibroMapper {
    LibroResponseDto toResponseDto(Libro libro);
}
