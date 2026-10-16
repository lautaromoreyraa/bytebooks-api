package com.bytebooks.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Order(-200)
public class FiltroDeLimiteDePedidos extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroDeLimiteDePedidos.class);

    private final IpDelCliente ipDelCliente;

    public FiltroDeLimiteDePedidos(IpDelCliente ipDelCliente) {
        this.ipDelCliente = ipDelCliente;
    }

    private final Cache<String, Bucket> loginBuckets = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES).build();

    private final Cache<String, Bucket> registerBuckets = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS).build();

    private final Cache<String, Bucket> generalBuckets = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES).build();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip     = ipDelCliente.de(request);
        String path   = request.getRequestURI();
        String method = request.getMethod();

        Bucket bucket;

        if ("POST".equals(method) && path.equals("/auth/login")) {
            bucket = loginBuckets.get(ip, k -> buildBucket(5, Duration.ofMinutes(1)));
        } else if ("POST".equals(method) && path.equals("/auth/register")) {
            bucket = registerBuckets.get(ip, k -> buildBucket(3, Duration.ofHours(1)));
        } else {
            bucket = generalBuckets.get(ip, k -> buildBucket(60, Duration.ofMinutes(1)));
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit excedido - ip={}, method={}, uri={}", ip, method, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"status\":429,\"message\":\"Demasiadas solicitudes. Intenta mas tarde.\"}");
        }
    }

    private Bucket buildBucket(long capacity, Duration refillPeriod) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillPeriod)
                        .build())
                .build();
    }

}
