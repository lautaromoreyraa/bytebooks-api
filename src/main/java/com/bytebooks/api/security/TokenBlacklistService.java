package com.bytebooks.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklist;

    public TokenBlacklistService(@Value("${jwt.expiration-ms}") long expirationMs) {
        this.blacklist = Caffeine.newBuilder()
                .expireAfterWrite(expirationMs, TimeUnit.MILLISECONDS)
                .build();
    }

    public void blacklist(String jti) {
        blacklist.put(jti, true);
    }

    public boolean isBlacklisted(String jti) {
        return blacklist.getIfPresent(jti) != null;
    }
}
