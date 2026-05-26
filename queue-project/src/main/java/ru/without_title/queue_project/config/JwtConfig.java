package ru.without_title.queue_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    public String getSecret() {
        return secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public String getHeader() {
        return header;
    }

    public String getPrefix() {
        return prefix;
    }
}