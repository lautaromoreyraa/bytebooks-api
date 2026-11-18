package com.bytebooks.api.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
