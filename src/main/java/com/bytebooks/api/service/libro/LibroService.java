package com.bytebooks.api.service.libro;

import com.bytebooks.api.domain.Libro;

import java.util.List;
import java.util.UUID;

public interface LibroService {
    Libro getLibroById(UUID id);
    List<Libro> obtenerTodosLosLibros();
    Libro agregarLibro(Libro libro);
    void eliminarLibro(UUID id);
    Libro actualizarLibro(Libro libro);
}
