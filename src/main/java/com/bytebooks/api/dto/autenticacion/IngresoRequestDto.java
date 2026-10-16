package com.bytebooks.api.dto.autenticacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record IngresoRequestDto(
        @NotBlank @Email
        String email,

        @NotBlank
        String password) { }
