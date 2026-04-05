package com.bytebooks.api.service.resena;

import com.bytebooks.api.dto.resena.ResenaRequestDto;
import com.bytebooks.api.dto.resena.ResenaResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ResenaService {
    Page<ResenaResponseDto> getResenasByLibro(UUID libroId, Pageable pageable);
    ResenaResponseDto crearResena(UUID libroId, UUID usuarioId, ResenaRequestDto request);
    ResenaResponseDto actualizarResena(UUID resenaId, UUID usuarioId, ResenaRequestDto request);
    void eliminarResena(UUID resenaId, UUID usuarioId);
}
