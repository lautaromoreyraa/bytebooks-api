package com.bytebooks.api.service.auth;

import com.bytebooks.api.dto.auth.AuthResponseDto;
import com.bytebooks.api.dto.auth.LoginRequestDto;
import com.bytebooks.api.dto.auth.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto request);
    AuthResponseDto login(LoginRequestDto request, String clientIp);
    void logout(String token);
}
