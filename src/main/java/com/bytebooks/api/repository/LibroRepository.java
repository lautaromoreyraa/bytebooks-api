package com.bytebooks.api.repository;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LibroRepository extends JpaRepository<Libro, UUID> {
    boolean existsByCategoria(Categoria categoria);
}
