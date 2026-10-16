package com.bytebooks.api.service.imagen;

import com.bytebooks.api.dto.imagen.FirmaDeSubidaResponseDto;

public interface FirmaDeSubidaService {

    /** Firma una subida a la carpeta configurada, valida por unos minutos. */
    FirmaDeSubidaResponseDto firmar();
}
