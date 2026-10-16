package com.bytebooks.api.service.autenticacion;

import com.bytebooks.api.dto.autenticacion.AutenticacionResponseDto;
import com.bytebooks.api.dto.autenticacion.IngresoRequestDto;
import com.bytebooks.api.dto.autenticacion.RegistroRequestDto;

public interface AutenticacionService {
    AutenticacionResponseDto register(RegistroRequestDto request);
    AutenticacionResponseDto login(IngresoRequestDto request, String clientIp);
    void logout(String token);
}
