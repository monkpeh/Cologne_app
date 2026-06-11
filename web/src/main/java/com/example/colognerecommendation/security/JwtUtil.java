package com.example.colognerecommendation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Generates and validates JWT tokens used to authenticate REST API requests.
 *
 * <p>Tokens are signed with HMAC-SHA256 using the secret configured in
 * {@code application.properties}. The secret must be at least 32 characters long.
 * Each token embeds the username as the subject and expires after the configured
 * duration (default 24 h).
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long      expiryMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiry-ms}") long expiryMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs  = expiryMs;
    }

    /** Generates a signed JWT token for the given user. */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(secretKey)
                .compact();
    }

    /** Extracts the username (subject) from the token without validating expiry. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** Returns {@code true} if the token is unexpired and belongs to the given user. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
