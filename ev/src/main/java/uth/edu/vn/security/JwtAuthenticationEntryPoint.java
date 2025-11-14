package uth.edu.vn.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Entry Point
 * Xử lý lỗi khi user không có quyền truy cập (401 Unauthorized)
 * Được gọi khi:
 * - Request không có JWT token
 * - JWT token không hợp lệ
 * - JWT token đã hết hạn
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        logger.error("Unauthorized error: {}", authException.getMessage());
        
        // Trả về HTTP 401 với message
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                          "Error: Unauthorized - " + authException.getMessage());
    }
}
