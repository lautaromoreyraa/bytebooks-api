package com.bytebooks.api.controller;

import com.bytebooks.api.dto.auth.AuthResponseDto;
import com.bytebooks.api.dto.auth.LoginRequestDto;
import com.bytebooks.api.dto.auth.RegisterRequestDto;
import com.bytebooks.api.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.bytebooks.api.security.IpDelCliente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final IpDelCliente ipDelCliente;

    public AuthController(AuthService authService, IpDelCliente ipDelCliente) {
        this.authService = authService;
        this.ipDelCliente = ipDelCliente;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request,
                                                  HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, ipDelCliente.de(httpRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

}
