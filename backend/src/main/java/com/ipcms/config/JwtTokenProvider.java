package com.ipcms.config;

import com.ipcms.model.Role;
import com.ipcms.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long clientExpirationMs;
    private final long internalExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.client-expiration-ms:28800000}") long clientExpirationMs,
            @Value("${app.jwt.internal-expiration-ms:43200000}") long internalExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.clientExpirationMs = clientExpirationMs;
        this.internalExpirationMs = internalExpirationMs;
    }

    /**
     * Appendix D: JWT active session tokens expire precisely 8 hours for standard client roles,
     * while internal underwriting/claims/admin operators match a 12-hour terminal boundary.
     */
    public String generateToken(User user) {
        long expiryDuration = isInternalRole(user.getRole()) ? internalExpirationMs : clientExpirationMs;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryDuration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("email", user.getEmail());
        claims.put("sessionType", isInternalRole(user.getRole()) ? "INTERNAL_12H" : "CLIENT_8H");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isInternalRole(Role role) {
        return role == Role.UNDERWRITER || role == Role.HANDLER || role == Role.ADMIN;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
