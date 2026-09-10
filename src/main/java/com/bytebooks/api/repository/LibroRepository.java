package com.bytebooks.api.repository;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.enumeration.EstadoLibroEnum;
import org.springframework.data.jpa.repository.JpaRepository;
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

    /** Para editar: el titulo puede repetirse consigo mismo, con nadie mas. */
    boolean existsByTituloAndIdNot(String titulo, UUID id);

    @Query("SELECT l.isbn FROM Libro l WHERE l.isbn IN :isbns")
    Set<String> findIsbnsByIsbnIn(@Param("isbns") List<String> isbns);

    @Query("SELECT l.titulo FROM Libro l")
    List<String> findAllTitulos();

    /*
     * Las tres consultas de abajo traen las categorias en el mismo viaje.
     * Libro.categorias es LAZY y el mapper las recorre siempre, asi que sin el
     * fetch cada libro sumaba su propia consulta: 26 libros costaban 34 viajes a
     * la base, y con 500 serian 500 por carga de la home.
     *
     * El ORDER BY tambien es nuevo: sin el, el orden de la grilla dependia de lo
     * que devolviera el motor, y un fetch join lo cambia sin avisar.
     *
     * Sin DISTINCT a proposito: el join contra la tabla de categorias repite el
     * libro una vez por categoria, pero Hibernate 6 ya descarta esos duplicados
     * al armar las entidades. Escribirlo mandaria un SELECT DISTINCT de verdad a
     * la base, que la obliga a comparar la sinopsis entera fila por fila.
     */

    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.categorias ORDER BY l.titulo")
    List<Libro> findAllConCategorias();

    /**
     * El estado se filtra en la consulta y no despues en memoria: traer el
     * catalogo entero para descartar la mitad crece con la tabla, no con lo que
     * se muestra. La comparacion contempla el null porque hay libros viejos
     * cargados antes de que el estado existiera, y en SQL {@code null <> 'OCULTO'}
     * no es verdadero: sin esa rama desaparecerian del catalogo.
     */
    @Query("""
            SELECT l FROM Libro l
            LEFT JOIN FETCH l.categorias
            WHERE l.estadoLibro IS NULL OR l.estadoLibro <> :oculto
            ORDER BY l.titulo
            """)
    List<Libro> findVisiblesConCategorias(@Param("oculto") EstadoLibroEnum oculto);

    @Query("SELECT l FROM Libro l LEFT JOIN FETCH l.categorias WHERE l.id = :id")
    Optional<Libro> findByIdConCategorias(@Param("id") UUID id);

    /**
     * Los favoritos salen por el mismo camino: el perfil es publico y llegaba a
     * pedir una consulta de categorias por libro guardado.
     */
    @Query("""
            SELECT l FROM Usuario u
            JOIN u.librosGuardados l
            LEFT JOIN FETCH l.categorias
            WHERE u.id = :usuarioId
            ORDER BY l.titulo
            """)
    List<Libro> findGuardadosPorUsuario(@Param("usuarioId") UUID usuarioId);
}
