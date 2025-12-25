package com.example.stockgestion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfigProperties {

    /**
     * Secret key for signing JWT tokens
     * Should be a strong, random string (at least 256 bits)
     */
    private String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    /**
     * Access token expiry time in milliseconds
     * Default: 15 minutes (900000 ms)
     */
    private long accessTokenExpiry = 900000L;

    /**
     * Refresh token expiry time in milliseconds
     * Default: 7 days (604800000 ms)
     */
    private long refreshTokenExpiry = 604800000L;
}
