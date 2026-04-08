package com.bytebooks.api.controller;

import com.bytebooks.api.dto.libro.LibroResponseDto;
import com.bytebooks.api.dto.usuario.ActualizarPerfilRequestDto;
import com.bytebooks.api.dto.usuario.UsuarioPerfilResponseDto;
import com.bytebooks.api.service.favorito.LibroFavoritoService;
import com.bytebooks.api.service.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final LibroFavoritoService favoritoService;

    public UsuarioController(UsuarioService usuarioService,
                             LibroFavoritoService favoritoService) {
        this.usuarioService  = usuarioService;
        this.favoritoService = favoritoService;
    }

    // Perfil público — sin autenticación
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioPerfilResponseDto> getPerfil(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.getPerfilById(id));
    }

    // Actualizar perfil propio
    @PutMapping("/me/perfil")
    public ResponseEntity<UsuarioPerfilResponseDto> actualizarPerfil(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ActualizarPerfilRequestDto request) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(UUID.fromString(userId), request));
    }

    // Favoritos públicos de un usuario
    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<LibroResponseDto>> getFavoritosDeUsuario(@PathVariable UUID id) {
        return ResponseEntity.ok(favoritoService.getFavoritosDeUsuario(id));
    }

    // Mis favoritos
    @GetMapping("/me/favoritos")
    public ResponseEntity<List<LibroResponseDto>> getMisFavoritos(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(favoritoService.getMisFavoritos(UUID.fromString(userId)));
    }

    @PostMapping("/me/favoritos/{libroId}")
    public ResponseEntity<Void> agregarFavorito(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID libroId) {
        favoritoService.agregarFavorito(UUID.fromString(userId), libroId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/favoritos/{libroId}")
    public ResponseEntity<Void> quitarFavorito(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID libroId) {
        favoritoService.quitarFavorito(UUID.fromString(userId), libroId);
        return ResponseEntity.noContent().build();
    }
}
