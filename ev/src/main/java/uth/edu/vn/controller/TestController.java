package uth.edu.vn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller
 * REST API endpoints để test các tính năng cơ bản
 */
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    public TestController() {
        System.out.println("=== TestController: CONSTRUCTOR CALLED ===");
    }
    
    /**
     * Test API hoạt động
     * GET /api/test/hello
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        System.out.println("=== TestController.hello() CALLED ===");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from EV Charging API!");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("status", "OK");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test CORS
     * GET /api/test/cors
     */
    @GetMapping("/cors")
    public ResponseEntity<Map<String, Object>> testCors() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS is working!");
        response.put("allowedOrigin", "*");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test Health Check
     * GET /api/test/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "EV Charging API");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ResponseEntity.ok(response);
    }
}