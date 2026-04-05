package com.bytebooks.api.dto.auth;

import java.util.UUID;

public record AuthResponseDto(
        UUID userId,
        String email,
        String accessToken,
        String tokenType,
        long expiresIn,
        String role) { }
