package com.bytebooks.api.repository;

import com.bytebooks.api.domain.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, UUID> {
    Page<Resena> findByLibroId(UUID libroId, Pageable pageable);
    Optional<Resena> findByLibroIdAndUsuarioId(UUID libroId, UUID usuarioId);
    void deleteByLibroId(UUID libroId);

    long countByLibroId(UUID libroId);

    /** Null si el libro no tiene resenas: AVG sobre cero filas no da cero, da nada. */
    @Query("SELECT AVG(r.puntuacion) FROM Resena r WHERE r.libro.id = :libroId")
    Double promedioDePuntuacion(@Param("libroId") UUID libroId);
}
