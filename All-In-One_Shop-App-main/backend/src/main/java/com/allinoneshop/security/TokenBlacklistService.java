package com.allinoneshop.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token blacklist for logout support.
 * Invalidated tokens are stored until they would have naturally expired.
 */
@Service
public class TokenBlacklistService {

    // Maps token -> expiration time (when the token would have expired naturally)
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist.
     *
     * @param token the JWT token to blacklist
     * @param expiresAt when the token would naturally expire
     */
    public void blacklist(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
        cleanup(); // opportunistically clean up expired entries
    }

    /**
     * Check if a token has been blacklisted.
     */
    public boolean isBlacklisted(String token) {
        Instant expiry = blacklist.get(token);
        if (expiry == null) return false;

        // If the token would have expired by now, remove it from the blacklist
        if (Instant.now().isAfter(expiry)) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /**
     * Remove expired entries from the blacklist.
     */
    private void cleanup() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }
}
