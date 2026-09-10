package com.bytebooks.api.fixture;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.enumeration.RolEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Usuarios armados para los tests. El email es unico por instancia. */
public final class FabricaDeUsuarios {

    private FabricaDeUsuarios() {
    }

    public static Usuario con(RolEnum rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Nombre");
        usuario.setApellido("Apellido");
        usuario.setEmail("usuaria-" + UUID.randomUUID() + "@ejemplo.test");
        // No pasa por el encoder: ningun test de los de aca inicia sesion.
        usuario.setPasswordHash("$2a$10$hashDePruebaQueNoSeVerifica");
        usuario.setRol(rol);
        usuario.setLibrosGuardados(new ArrayList<>());
        return usuario;
    }

    public static Usuario conFavoritos(RolEnum rol, List<Libro> favoritos) {
        Usuario usuario = con(rol);
        usuario.setLibrosGuardados(new ArrayList<>(favoritos));
        return usuario;
    }
}
