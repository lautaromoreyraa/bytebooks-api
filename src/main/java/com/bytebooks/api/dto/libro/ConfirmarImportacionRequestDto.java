package com.bytebooks.api.dto.libro;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ConfirmarImportacionRequestDto(
        @NotEmpty(message = "Debe seleccionar al menos un libro")
        List<GoogleBookCandidateDto> libros,

        @NotNull(message = "Debe seleccionar una categoría")
        UUID categoriaId
) { }
