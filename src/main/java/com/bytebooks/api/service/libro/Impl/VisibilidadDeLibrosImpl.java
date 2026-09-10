package com.bytebooks.api.service.libro.Impl;

import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.service.libro.VisibilidadDeLibros;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class VisibilidadDeLibrosImpl implements VisibilidadDeLibros {

    @Override
    public boolean esVisible(Libro libro) {
        return libro.getEstadoLibro() != EstadoLibroEnum.OCULTO || puedeGestionar();
    }

    @Override
    public boolean puedeGestionar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // El token anonimo de Spring tambien responde true a isAuthenticated(),
        // asi que lo que decide de verdad es el rol, no el flag.
        return authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        RolEnum.ROLE_ADMIN.name().equals(authority.getAuthority())
                                || RolEnum.ROLE_MODERATOR.name().equals(authority.getAuthority()));
    }
}
