package com.bytebooks.api.service.favorito;

import com.bytebooks.api.dto.libro.LibroResponseDto;

import java.util.List;
import java.util.UUID;

public interface LibroFavoritoService {
    List<LibroResponseDto> getMisFavoritos(UUID usuarioId);
    List<LibroResponseDto> getFavoritosDeUsuario(UUID usuarioId);
    void agregarFavorito(UUID usuarioId, UUID libroId);
    void quitarFavorito(UUID usuarioId, UUID libroId);
}
