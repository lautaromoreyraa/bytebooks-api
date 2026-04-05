package com.bytebooks.api.security;

import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.enumeration.RolEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    public UUID getId()     { return usuario.getId(); }
    public RolEnum getRol() { return usuario.getRol(); }

    @Override
    public String getUsername() { return usuario.getEmail(); }

    @Override
    public String getPassword() { return usuario.getPasswordHash(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(usuario.getRol().name()));
    }
}
