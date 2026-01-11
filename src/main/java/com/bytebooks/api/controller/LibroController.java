package com.bytebooks.api.controller;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.service.libro.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/libro")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @PostMapping
    public Libro agregarLibro(@RequestBody Libro libro) {
        return libroService.agregarLibro(libro);
    }

}
