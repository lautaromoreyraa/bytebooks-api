package com.bytebooks.api.repository;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LibroRepository extends JpaRepository<Libro, UUID> {
    boolean existsByCategorias(Categoria categoria);

    @Query("SELECT l.isbn FROM Libro l WHERE l.isbn IN :isbns")
    Set<String> findIsbnsByIsbnIn(@Param("isbns") List<String> isbns);

    @Query("SELECT l.titulo FROM Libro l")
    List<String> findAllTitulos();
}
