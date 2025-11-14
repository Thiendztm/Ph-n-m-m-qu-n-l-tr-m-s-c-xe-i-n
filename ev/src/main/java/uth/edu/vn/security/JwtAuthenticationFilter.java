package uth.edu.vn.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Filter
 * Filter này chạy trước mỗi request để:
 * 1. Đọc JWT token từ header "Authorization: Bearer <token>"
 * 2. Validate token
 * 3. Load user và set vào SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            logger.info("Processing request: {}", requestURI);
            
            // 1. Lấy JWT từ request header
            String jwt = getJwtFromRequest(request);
            logger.info("JWT token found: {}", jwt != null ? "YES" : "NO");
            
            // 2. Validate và lấy username
            if (StringUtils.hasText(jwt)) {
                logger.info("Validating JWT token...");
                boolean isValid = tokenProvider.validateToken(jwt);
                logger.info("JWT token valid: {}", isValid);
                
                if (isValid) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    logger.info("Username from token: {}", username);
                    
                    // 3. Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.info("User details loaded: {}", userDetails != null);
                    
                    if (userDetails != null) {
                        // 4. Tạo Authentication object
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                            );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 5. Set vào SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.info("Set authentication for user: {}", username);
                    } else {
                        logger.warn("User details not found for username: {}", username);
                    }
                } else {
                    logger.warn("JWT token validation failed");
                }
            } else {
                logger.warn("No JWT token found in request");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        
        // 6. Continue filter chain
        logger.info("Continuing filter chain for request: {}", request.getRequestURI());
        filterChain.doFilter(request, response);
        logger.info("Filter chain completed for request: {}", request.getRequestURI());
    }
    
    /**
     * Lấy JWT token từ header "Authorization: Bearer <token>"
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check if header contains "Bearer <token>"
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }
}
