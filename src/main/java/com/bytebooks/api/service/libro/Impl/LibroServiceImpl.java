package com.bytebooks.api.service.libro.Impl;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.repository.LibroRepository;
import com.bytebooks.api.service.libro.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LibroServiceImpl implements LibroService {

    @Autowired
    private LibroRepository libroRepository;

    @Override
    public Libro getLibroById(UUID id) {
        return libroRepository.getReferenceById(id);
    }

    @Override
    public List<Libro> obtenerTodosLosLibros() {
        return libroRepository.findAll();
    }

    @Override
    public Libro agregarLibro(Libro libro) {
        return libroRepository.save(libro);
    }

    @Override
    public void eliminarLibro(UUID id) {
        libroRepository.deleteById(id);
    }

    @Override
    public Libro actualizarLibro(Libro libro) {
        return libroRepository.save(libro);
    }
}
