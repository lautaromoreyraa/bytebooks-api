package com.bytebooks.api.security;

import com.bytebooks.api.client.TurnstileClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Exige el desafio de Turnstile en el registro, el unico formulario abierto que
 * crea algo en la base.
 *
 * El limite por IP de {@link FiltroDeLimiteDePedidos} frena a un script que
 * insiste desde una sola maquina; este frena al que reparte los pedidos entre
 * muchas IPs. Va sin {@code @Order}, despues de la cadena de Spring Security:
 * asi el 403 sale con las cabeceras de CORS y el navegador puede leer el
 * mensaje en lugar de reportar un error de red.
 */
@Component
public class FiltroDeTurnstile extends OncePerRequestFilter {

    static final String CABECERA_TOKEN = "Cf-Turnstile-Response";

    private final TurnstileClient turnstileClient;
    private final IpDelCliente ipDelCliente;

    public FiltroDeTurnstile(TurnstileClient turnstileClient, IpDelCliente ipDelCliente) {
        this.turnstileClient = turnstileClient;
        this.ipDelCliente = ipDelCliente;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && "/auth/register".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (turnstileClient.valido(request.getHeader(CABECERA_TOKEN), ipDelCliente.de(request))) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":403,\"message\":"
                + "\"No pudimos verificar que seas una persona. Recarga la pagina e intenta de nuevo.\"}");
    }
}
