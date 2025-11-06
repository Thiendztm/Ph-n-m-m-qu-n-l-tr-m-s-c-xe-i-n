package uth.edu.vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.dto.auth.AuthResponse;
import uth.edu.vn.dto.auth.LoginRequest;
import uth.edu.vn.dto.auth.RegisterRequest;
import uth.edu.vn.service.AuthService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * REST API endpoints cho đăng ký, đăng nhập, refresh token
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Đăng ký user mới (EV Driver)
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Đăng nhập
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refresh access token bằng refresh token
     * POST /api/auth/refresh
     * Body: { "refreshToken": "eyJhbGc..." }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint - check authentication status
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        // TODO: Implement get current authenticated user
        Map<String, String> response = new HashMap<>();
        response.put("message", "Authentication endpoint working");
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout (client-side only - xóa token khỏi localStorage)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng xuất thành công. Vui lòng xóa token ở client.");
        return ResponseEntity.ok(response);
    }
}
