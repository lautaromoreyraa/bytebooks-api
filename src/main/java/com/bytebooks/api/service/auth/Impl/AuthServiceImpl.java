package com.bytebooks.api.service.auth.Impl;

import com.bytebooks.api.domain.Usuario;
import com.bytebooks.api.dto.auth.AuthResponseDto;
import com.bytebooks.api.dto.auth.LoginRequestDto;
import com.bytebooks.api.dto.auth.RegisterRequestDto;
import com.bytebooks.api.enumeration.RolEnum;
import com.bytebooks.api.repository.UsuarioRepository;
import com.bytebooks.api.security.LoginAttemptService;
import com.bytebooks.api.security.TokenBlacklistService;
import com.bytebooks.api.security.UsuarioPrincipal;
import com.bytebooks.api.security.jwt.JwtUtil;
import com.bytebooks.api.service.auth.AuthService;
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
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           LoginAttemptService loginAttemptService,
                           TokenBlacklistService tokenBlacklistService) {
        this.usuarioRepository     = usuarioRepository;
        this.passwordEncoder       = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil               = jwtUtil;
        this.loginAttemptService   = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {
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
        return new AuthResponseDto(usuario.getId(), usuario.getEmail(), token, "Bearer", jwtUtil.getExpirationMs(), usuario.getRol().name());
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request, String clientIp) {
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
            return new AuthResponseDto(principal.getId(), principal.getUsername(), token, "Bearer", jwtUtil.getExpirationMs(), principal.getRol().name());

        } catch (BadCredentialsException | DisabledException | LockedException e) {
            loginAttemptService.loginFailed(clientIp);
            log.warn("Login fallido - ip={}, razon={}", clientIp, e.getClass().getSimpleName());
            throw new IllegalArgumentException("Credenciales invalidas");
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
