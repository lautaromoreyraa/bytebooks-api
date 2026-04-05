package com.bytebooks.api.dto.libro;

public record BookImportResponseDto(
        String query,
        int encontrados,
        int guardados,
        int descartadosPorDuplicado,
        int descartadosPorDatosInvalidos,
        int descartadosPorError
) { }
