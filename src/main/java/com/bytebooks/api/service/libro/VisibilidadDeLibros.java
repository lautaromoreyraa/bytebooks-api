package com.bytebooks.api.service.libro;

import com.bytebooks.api.domain.Libro;

/**
 * Decide quien puede ver un libro OCULTO.
 *
 * Vive aparte del servicio de libros porque no es el unico lugar que expone
 * libros: el perfil publico devuelve los favoritos de cualquiera sin pedir
 * sesion. Mientras la regla estuvo privada dentro de LibroServiceImpl, el
 * catalogo filtraba bien y los favoritos publicaban el libro igual.
 */
public interface VisibilidadDeLibros {

    /** Falso solo si el libro esta OCULTO y quien mira no puede gestionarlo. */
    boolean esVisible(Libro libro);

    /** Admin o moderador: los roles que necesitan ver los ocultos para editarlos. */
    boolean puedeGestionar();
}
