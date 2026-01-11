package com.bytebooks.api.repository;

import com.bytebooks.api.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface usuarioRepository extends JpaRepository<Usuario, UUID> {
}
