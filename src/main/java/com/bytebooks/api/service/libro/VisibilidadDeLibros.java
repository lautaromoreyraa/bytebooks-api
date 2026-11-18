package com.bytebooks.api.service.libro;

import com.bytebooks.api.domain.Libro;

public interface VisibilidadDeLibros {

    boolean esVisible(Libro libro);

    boolean puedeGestionar();
}
