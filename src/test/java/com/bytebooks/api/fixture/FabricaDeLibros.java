package com.bytebooks.api.fixture;

import com.bytebooks.api.domain.Categoria;
import com.bytebooks.api.domain.Libro;
import com.bytebooks.api.enumeration.EstadoLibroEnum;

import java.util.Set;
import java.util.UUID;

/**
 * Libros armados para los tests.
 *
 * Cada metodo devuelve un libro valido y completo: si un test necesita que un
 * campo tenga cierto valor, lo pisa despues. Asi lo unico que aparece escrito
 * en el test es lo que ese test esta probando, y el resto no distrae.
 */
public final class FabricaDeLibros {

    private FabricaDeLibros() {
    }

    public static Categoria categoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.setNombre(nombre);
        return categoria;
    }

    public static Libro disponible(String titulo) {
        Libro libro = new Libro();
        libro.setId(UUID.randomUUID());
        libro.setTitulo(titulo);
        libro.setAutor("Autora de prueba");
        libro.setDescripcion("Sinopsis de prueba.");
        libro.setCategorias(Set.of(categoria("Narrativa")));
        libro.setEditorial("Editorial de prueba");
        libro.setAnioPublicacion("1998");
        libro.setIsbn(isbnAleatorio());
        libro.setPortada("https://ejemplo.test/portada.jpg");
        libro.setEstadoLibro(EstadoLibroEnum.DISPONIBLE);
        return libro;
    }

    public static Libro oculto(String titulo) {
        Libro libro = disponible(titulo);
        libro.setEstadoLibro(EstadoLibroEnum.OCULTO);
        return libro;
    }

    /**
     * La columna isbn es UNIQUE: dos libros de la fabrica dentro del mismo test
     * chocarian si compartieran valor fijo.
     */
    public static String isbnAleatorio() {
        long numero = Math.abs(UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L);
        return String.format("978%010d", numero);
    }
}
