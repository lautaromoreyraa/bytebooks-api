package com.bytebooks.api.repository;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LibroRepository extends JpaRepository<Libro, UUID> {
    boolean existsByCategorias(Categoria categoria);

    boolean existsByTitulo(String titulo);

    boolean existsByTituloAndIdNot(String titulo, UUID id);

    @Query("SELECT l.isbn FROM Libro l WHERE l.isbn IN :isbns")
    Set<String> findIsbnsByIsbnIn(@Param("isbns") List<String> isbns);

    @Query("SELECT l.titulo FROM Libro l")
    List<String> findAllTitulos();


    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.categorias ORDER BY l.titulo")
    List<Libro> findAllConCategorias();

    @Query("""
            SELECT l FROM Libro l
            LEFT JOIN FETCH l.categorias
            WHERE l.estadoLibro IS NULL OR l.estadoLibro <> :oculto
            ORDER BY l.titulo
            """)
    List<Libro> findVisiblesConCategorias(@Param("oculto") EstadoLibroEnum oculto);

    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.categorias WHERE l.id = :id")
    Optional<Libro> findByIdConCategorias(@Param("id") UUID id);

    @Query("""
            SELECT l FROM Usuario u
            JOIN u.librosGuardados l
            LEFT JOIN FETCH l.categorias
            WHERE u.id = :usuarioId
            ORDER BY l.titulo
            """)
    List<Libro> findGuardadosPorUsuario(@Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query(value = "DELETE FROM usuario_libros_guardados WHERE libro_id = :libroId", nativeQuery = true)
    void quitarDeFavoritos(@Param("libroId") String libroId);
}
