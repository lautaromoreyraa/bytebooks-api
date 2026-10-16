package com.bytebooks.api.dto.autenticacion;

import java.util.UUID;

public record AutenticacionResponseDto(
        UUID userId,
        String email,
        String accessToken,
        String tokenType,
        long expiresIn,
        String role) { }
