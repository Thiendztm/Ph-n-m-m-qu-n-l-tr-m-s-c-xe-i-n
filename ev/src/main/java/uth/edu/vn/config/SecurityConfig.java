package uth.edu.vn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uth.edu.vn.security.JwtAuthenticationEntryPoint;
import uth.edu.vn.security.JwtAuthenticationFilter;
import uth.edu.vn.security.UserDetailsServiceImpl;

import java.util.Arrays;

/**
 * Spring Security Configuration
 * Cấu hình bảo mật cho toàn bộ ứng dụng:
 * - JWT authentication
 * - CORS policy
 * - Public/Protected endpoints
 * - Password encoding
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true  // Enable @PreAuthorize, @PostAuthorize
)
public class SecurityConfig {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    @Value("${cors.allowed-origins:http://localhost:8080}")
    private String[] allowedOrigins;
    
    /**
     * JWT Authentication Filter Bean
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    /**
     * Password Encoder Bean - BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * DaoAuthenticationProvider Bean
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Authentication Manager Bean (mới cho Spring Security 5.7+)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * Configure HTTP Security (SecurityFilterChain cho Spring Security 6)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS configuration - new syntax
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF disabled vì dùng JWT (stateless)
            .csrf(csrf -> csrf.disable())
            
            // Exception handling - new syntax
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            
            // Session management - STATELESS (không dùng session)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules - new syntax
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - không cần authentication
                .requestMatchers(
                    "/",                      // Root page
                    "/index.html",            // Home page
                    "/login.html",            // Login page
                    "/register.html",         // Register page
                    "/admin/**",              // Admin frontend pages
                    "/staff/**",              // Staff frontend pages
                    "/src/**",                // Static resources (CSS, JS, images)
                    "/api/auth/**",           // Register, login, refresh token
                    "/api/health",            // Health check
                    "/api/stations/public/**", // Public station info
                    "/ws/**"                  // WebSocket endpoint
                ).permitAll()
                
                // EV Driver endpoints
                .requestMatchers("/api/driver/**").hasRole("EV_DRIVER")
                .requestMatchers("/api/bookings/**").hasRole("EV_DRIVER")
                .requestMatchers("/api/sessions/**").hasRole("EV_DRIVER")
                .requestMatchers("/api/wallet/**").hasRole("EV_DRIVER")
                
                // CS Staff endpoints
                .requestMatchers("/api/staff/**").hasRole("CS_STAFF")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            );
        
        // Set authentication provider
        http.authenticationProvider(authenticationProvider());
        
        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build(); // Return SecurityFilterChain
    }

    /**
     * CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins (frontend URLs)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Max age
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
