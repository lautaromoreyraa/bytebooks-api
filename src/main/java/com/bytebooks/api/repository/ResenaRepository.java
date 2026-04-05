package com.bytebooks.api.repository;

import com.bytebooks.api.domain.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, UUID> {
    Page<Resena> findByLibroId(UUID libroId, Pageable pageable);
    Optional<Resena> findByLibroIdAndUsuarioId(UUID libroId, UUID usuarioId);
    void deleteByLibroId(UUID libroId);
}
