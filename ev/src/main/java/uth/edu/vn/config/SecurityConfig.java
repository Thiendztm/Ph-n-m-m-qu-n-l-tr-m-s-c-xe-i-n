package uth.edu.vn.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true  // Enable @PreAuthorize, @PostAuthorize
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    @Value("${cors.allowed-origins}")
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
     * Authentication Manager Bean
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    /**
     * Configure AuthenticationManager
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }
    
    /**
     * Configure HTTP Security
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // CORS configuration
            .cors()
            .and()
            
            // CSRF disabled vì dùng JWT (stateless)
            .csrf().disable()
            
            // Exception handling
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
            .and()
            
            // Session management - STATELESS (không dùng session)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // Authorization rules
            .authorizeRequests()
                // Public endpoints - không cần authentication
                .antMatchers(
                    "/api/auth/**",           // Register, login, refresh token
                    "/api/health",            // Health check
                    "/api/stations/public/**", // Public station info
                    "/ws/**"                  // WebSocket endpoint
                ).permitAll()
                
                // EV Driver endpoints
                .antMatchers("/api/driver/**").hasRole("EV_DRIVER")
                .antMatchers("/api/bookings/**").hasRole("EV_DRIVER")
                .antMatchers("/api/sessions/**").hasRole("EV_DRIVER")
                .antMatchers("/api/wallet/**").hasRole("EV_DRIVER")
                
                // CS Staff endpoints
                .antMatchers("/api/staff/**").hasRole("CS_STAFF")
                
                // Admin endpoints
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other requests need authentication
                .anyRequest().authenticated();
        
        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
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
