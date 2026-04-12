package ru.without_title.queue_project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import ru.without_title.queue_project.config.JwtConfig;
import ru.without_title.queue_project.database.entities.enums.SystemRole;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenUtil {

    private final JwtConfig jwtConfig;
    private SecretKey secretKey;

    public JwtTokenUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    // Ленивая инициализация ключа (аналог Kotlin by lazy)
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        }
        return secretKey;
    }

    // Генерация токена
    public String generateToken(Long userId, String email, SystemRole role) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getSecretKey())
                .compact();
    }

    // Извлечение claims из токена
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Проверка токена
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Извлечение email
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // Извлечение userId
    public Long getUserIdFromToken(String token) {
        Number userId = getClaims(token).get("userId", Number.class);
        return userId.longValue();
    }

    // Извлечение роли
    public SystemRole getRoleFromToken(String token) {
        String roleName = getClaims(token).get("role", String.class);
        return SystemRole.valueOf(roleName);
    }
}
