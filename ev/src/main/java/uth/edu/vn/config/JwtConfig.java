package uth.edu.vn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration
 * Chứa các thông số cấu hình cho JWT token
 */
@Configuration
public class JwtConfig {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration; // milliseconds
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration; // milliseconds
    
    public String getSecret() {
        return secret;
    }
    
    public Long getExpiration() {
        return expiration;
    }
    
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}
