package com.bytebooks.api.controller;

import com.bytebooks.api.dto.autenticacion.AutenticacionResponseDto;
import com.bytebooks.api.dto.autenticacion.IngresoRequestDto;
import com.bytebooks.api.dto.autenticacion.RegistroRequestDto;
import com.bytebooks.api.service.autenticacion.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.bytebooks.api.security.IpDelCliente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutenticacionController {

    private final AutenticacionService authService;

    private final IpDelCliente ipDelCliente;

    public AutenticacionController(AutenticacionService authService, IpDelCliente ipDelCliente) {
        this.authService = authService;
        this.ipDelCliente = ipDelCliente;
    }

    @PostMapping("/register")
    public ResponseEntity<AutenticacionResponseDto> register(@Valid @RequestBody RegistroRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AutenticacionResponseDto> login(@Valid @RequestBody IngresoRequestDto request,
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
