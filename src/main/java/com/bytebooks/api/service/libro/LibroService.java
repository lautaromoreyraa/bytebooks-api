package com.bytebooks.api.service.libro;

import com.bytebooks.api.dto.libro.LibroRequestDto;
import com.bytebooks.api.dto.libro.LibroResponseDto;

import java.util.List;
import java.util.UUID;

public interface LibroService {
    LibroResponseDto getLibroById(UUID id);
    List<LibroResponseDto> getAllLibros();
    LibroResponseDto agregarLibro(LibroRequestDto request);
    LibroResponseDto actualizarLibro(UUID id, LibroRequestDto request);
    void eliminarLibro(UUID id);
}
