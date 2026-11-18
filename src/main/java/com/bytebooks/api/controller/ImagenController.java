package com.bytebooks.api.controller;

import com.bytebooks.api.dto.imagen.FirmaDeSubidaResponseDto;
import com.bytebooks.api.service.imagen.FirmaDeSubidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/imagenes")
public class ImagenController {

    private final FirmaDeSubidaService firmaDeSubidaService;

    public ImagenController(FirmaDeSubidaService firmaDeSubidaService) {
        this.firmaDeSubidaService = firmaDeSubidaService;
    }

    @PostMapping("/firma")
    public ResponseEntity<FirmaDeSubidaResponseDto> firmar() {
        return ResponseEntity.ok(firmaDeSubidaService.firmar());
    }
}
