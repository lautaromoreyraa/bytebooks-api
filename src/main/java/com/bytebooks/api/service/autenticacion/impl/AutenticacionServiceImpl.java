package com.bytebooks.api.service.autenticacion.impl;

import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.dto.autenticacion.AutenticacionResponseDto;
import com.bytebooks.api.dto.autenticacion.IngresoRequestDto;
import com.bytebooks.api.dto.autenticacion.RegistroRequestDto;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.repository.UsuarioRepository;
import com.bytebooks.api.security.ControlDeIntentosDeIngreso;
import com.bytebooks.api.exception.CredencialesInvalidasException;
import com.bytebooks.api.security.ListaDeTokensRevocados;
import com.bytebooks.api.security.UsuarioPrincipal;
import com.bytebooks.api.security.jwt.ProveedorDeTokensJwt;
import com.bytebooks.api.service.autenticacion.AutenticacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionServiceImpl implements AutenticacionService {

    private static final Logger log = LoggerFactory.getLogger(AutenticacionServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ProveedorDeTokensJwt jwtUtil;
    private final ControlDeIntentosDeIngreso loginAttemptService;
    private final ListaDeTokensRevocados tokenBlacklistService;

    public AutenticacionServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           ProveedorDeTokensJwt jwtUtil,
                           ControlDeIntentosDeIngreso loginAttemptService,
                           ListaDeTokensRevocados tokenBlacklistService) {
        this.usuarioRepository     = usuarioRepository;
        this.passwordEncoder       = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil               = jwtUtil;
        this.loginAttemptService   = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public AutenticacionResponseDto register(RegistroRequestDto request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            // Mensaje generico para no revelar si el email existe (A06 - enumeracion)
            throw new IllegalStateException("No se pudo completar el registro con los datos proporcionados");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(RolEnum.ROLE_USER);

        usuarioRepository.save(usuario);
        log.info("Nuevo usuario registrado - userId={}", usuario.getId());

        String token = jwtUtil.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
        return new AutenticacionResponseDto(usuario.getId(), usuario.getEmail(), token, "Bearer", jwtUtil.getExpirationMs(), usuario.getRol().name());
    }

    @Override
    public AutenticacionResponseDto login(IngresoRequestDto request, String clientIp) {
        if (loginAttemptService.isBlocked(clientIp)) {
            log.warn("Login bloqueado por exceso de intentos - ip={}", clientIp);
            throw new IllegalStateException("Demasiados intentos fallidos. Intenta en 15 minutos.");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            loginAttemptService.loginSucceeded(clientIp);

            UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
            log.info("Login exitoso - userId={}, ip={}", principal.getId(), clientIp);

            String token = jwtUtil.generateToken(principal.getId(), principal.getUsername(), principal.getRol());
            return new AutenticacionResponseDto(principal.getId(), principal.getUsername(), token, "Bearer", jwtUtil.getExpirationMs(), principal.getRol().name());

        } catch (BadCredentialsException | DisabledException | LockedException e) {
            loginAttemptService.loginFailed(clientIp);
            log.warn("Login fallido - ip={}, razon={}", clientIp, e.getClass().getSimpleName());
            throw new CredencialesInvalidasException("Credenciales invalidas");
        }
    }

    @Override
    public void logout(String token) {
        if (jwtUtil.isTokenValid(token)) {
            String jti    = jwtUtil.extractJti(token);
            String userId = jwtUtil.extractSubject(token);
            tokenBlacklistService.blacklist(jti);
            log.info("Logout - userId={}, jti={}", userId, jti);
        }
    }
}
