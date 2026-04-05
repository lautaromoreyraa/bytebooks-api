package com.bytebooks.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPTS   = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(LOCKOUT_MINUTES, TimeUnit.MINUTES)
            .build();

    public void loginSucceeded(String ip) {
        attemptsCache.invalidate(ip);
    }

    public void loginFailed(String ip) {
        int attempts = getAttempts(ip) + 1;
        attemptsCache.put(ip, attempts);
        if (attempts >= MAX_ATTEMPTS) {
            log.warn("IP bloqueada por exceso de intentos fallidos - ip={}, intentos={}", ip, attempts);
        }
    }

    public boolean isBlocked(String ip) {
        return getAttempts(ip) >= MAX_ATTEMPTS;
    }

    private int getAttempts(String ip) {
        Integer attempts = attemptsCache.getIfPresent(ip);
        return attempts == null ? 0 : attempts;
    }
}
