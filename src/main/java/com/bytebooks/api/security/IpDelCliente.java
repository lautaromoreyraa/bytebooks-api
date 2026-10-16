package com.bytebooks.api.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * La IP real de quien hace el pedido, para el limite de pedidos y el bloqueo
 * por intentos fallidos de login.
 *
 * Detras de Cloudflare se lee de {@code CF-Connecting-IP}, que Cloudflare
 * escribe siempre y pisa si el cliente lo manda. No se usa el primer valor de
 * {@code X-Forwarded-For}: ese header lo puede inventar el cliente, y
 * cambiandolo en cada intento se salteaban los dos limites.
 */
@Component
public class IpDelCliente {

    private final boolean detrasDeCloudflare;

    public IpDelCliente(@Value("${app.security.trust-proxy:false}") boolean detrasDeCloudflare) {
        this.detrasDeCloudflare = detrasDeCloudflare;
    }

    public String de(HttpServletRequest request) {
        if (detrasDeCloudflare) {
            String ip = request.getHeader("CF-Connecting-IP");
            if (ip != null && !ip.isBlank()) {
                return ip.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
