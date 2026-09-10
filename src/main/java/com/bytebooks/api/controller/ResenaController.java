package com.bytebooks.api.controller;

import com.bytebooks.api.dto.resena.ResenaRequestDto;
import com.bytebooks.api.dto.resena.ResenaResponseDto;
import com.bytebooks.api.dto.resena.ResumenDeResenasDto;
import com.bytebooks.api.service.resena.ResenaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    /**
     * La direccion es explicita: sin ella Spring ordena ascendente y la primera
     * pagina son las diez resenas mas viejas. Ademas de estar al reves para quien
     * lee, hacia que una resena recien publicada apareciera arriba y al recargar
     * saltara al final de la ultima pagina.
     */
    @GetMapping("/libros/{libroId}/resenas")
    public ResponseEntity<Page<ResenaResponseDto>> getResenasByLibro(
            @PathVariable UUID libroId,
            @PageableDefault(size = 10, sort = "fechaResena", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(resenaService.getResenasByLibro(libroId, pageable));
    }

    /**
     * Promedio y total sobre la tabla entera. El cliente los sacaba de la primera
     * pagina, asi que un libro con mas de diez resenas mostraba un promedio de
     * una muestra o directamente ninguno.
     */
    @GetMapping("/libros/{libroId}/resenas/resumen")
    public ResponseEntity<ResumenDeResenasDto> getResumenDeResenas(@PathVariable UUID libroId) {
        return ResponseEntity.ok(resenaService.getResumenByLibro(libroId));
    }

    @PostMapping("/libros/{libroId}/resenas")
    public ResponseEntity<ResenaResponseDto> crearResena(
            @PathVariable UUID libroId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ResenaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resenaService.crearResena(libroId, UUID.fromString(userId), request));
    }

    @PutMapping("/resenas/{id}")
    public ResponseEntity<ResenaResponseDto> actualizarResena(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ResenaRequestDto request) {
        return ResponseEntity.ok(resenaService.actualizarResena(id, UUID.fromString(userId), request));
    }

    @DeleteMapping("/resenas/{id}")
    public ResponseEntity<Void> eliminarResena(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        resenaService.eliminarResena(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
