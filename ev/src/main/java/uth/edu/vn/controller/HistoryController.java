package uth.edu.vn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    
    // Constructor để log khi Spring tạo instance
    public HistoryController() {
        System.out.println("=== HistoryController: CONSTRUCTOR CALLED - Spring is loading this controller ===");
        System.out.println("=== HistoryController: Mapping = /api/history ===");
    }
    
    @GetMapping("/charging")
    public ResponseEntity<String> getChargingHistory() {
        System.out.println("=== HistoryController.getChargingHistory() CALLED - URL: /api/history/charging ===");
        try {
            System.out.println("=== HistoryController.getChargingHistory() - Processing request ===");
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body("{\"message\":\"Charging history endpoint working!\",\"status\":\"success\",\"data\":[]}");
        } catch (Exception e) {
            System.err.println("=== HistoryController.getChargingHistory() ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .header("Content-Type", "application/json")
                .body("{\"error\":\"" + e.getMessage() + "\",\"status\":\"error\"}");
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("=== HistoryController.test() CALLED ===");
        return ResponseEntity.ok("History test endpoint working!");
    }
    
    @GetMapping("/debug")
    public ResponseEntity<String> debug() {
        System.out.println("=== HistoryController.debug() CALLED - This should appear in logs ===");
        return ResponseEntity.ok("Debug endpoint from HistoryController - working!");
    }
}