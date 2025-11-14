package uth.edu.vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.entity.User;
import uth.edu.vn.entity.Xe;
import uth.edu.vn.repository.UserRepository;
import uth.edu.vn.repository.XeRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private XeRepository xeRepository;
    
    // Constructor để log khi Spring tạo instance
    public ProfileController() {
        System.out.println("=== ProfileController: CONSTRUCTOR CALLED - Spring is loading this controller ===");
    }
    
    /**
     * Test endpoint đơn giản
     */
    @GetMapping
    public ResponseEntity<String> getProfile(Authentication auth) {
        try {
            System.out.println("=== ProfileController.getProfile() CALLED ===");
            
            // Lấy email từ JWT token
            String userEmail = auth.getName();
            System.out.println("Email from JWT: " + userEmail);
            
            // Tìm user trong database
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Tìm xe của user (nếu có)
            List<Xe> userVehicles = xeRepository.findByUserId(user.getId());
            Optional<Xe> userVehicle = userVehicles.isEmpty() ? Optional.empty() : Optional.of(userVehicles.get(0));
            
            // Tạo JSON response với dữ liệu thực
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"status\":\"success\",");
            json.append("\"user\":{");
            json.append("\"id\":").append(user.getId()).append(",");
            json.append("\"email\":\"").append(user.getEmail()).append("\",");
            json.append("\"firstName\":\"").append(user.getFirstName() != null ? user.getFirstName() : "").append("\",");
            json.append("\"lastName\":\"").append(user.getLastName() != null ? user.getLastName() : "").append("\",");
            json.append("\"phone\":\"").append(user.getPhone() != null ? user.getPhone() : "").append("\",");
            json.append("\"role\":\"").append(user.getRole().toString()).append("\",");
            json.append("\"walletBalance\":").append(user.getWalletBalance() != null ? user.getWalletBalance() : 0);
            json.append("},");
            
            if (userVehicle.isPresent()) {
                Xe vehicle = userVehicle.get();
                json.append("\"vehicle\":{");
                json.append("\"make\":\"").append(vehicle.getMake() != null ? vehicle.getMake() : "").append("\",");
                json.append("\"model\":\"").append(vehicle.getModel() != null ? vehicle.getModel() : "").append("\",");
                json.append("\"plateNumber\":\"").append(vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "").append("\",");
                json.append("\"plugType\":\"").append(vehicle.getPlugType() != null ? vehicle.getPlugType() : "").append("\"");
                json.append("}");
            } else {
                json.append("\"vehicle\":null");
            }
            
            json.append("}");
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(json.toString());
                
        } catch (Exception e) {
            System.err.println("=== ProfileController.getProfile() ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .header("Content-Type", "application/json")
                .body("{\"error\":\"" + e.getMessage().replace("\"", "\\\"" ) + "\",\"status\":\"error\"}");
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("=== ProfileController.test() CALLED ===");
        return ResponseEntity.ok("Profile test endpoint working!");
    }
    
    /**
     * GET /api/profile/vehicle - Lấy thông tin xe
     */
    @GetMapping("/vehicle")
    public ResponseEntity<String> getVehicle() {
        try {
            System.out.println("=== ProfileController.getVehicle() CALLED ===");
            return ResponseEntity.ok("{\"message\":\"Vehicle endpoint working!\"}");
        } catch (Exception e) {
            System.err.println("=== ProfileController.getVehicle() ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * PUT /api/profile/vehicle - Cập nhật thông tin xe
     */
    @PutMapping("/vehicle")
    public ResponseEntity<String> updateVehicle(@RequestBody String vehicleData) {
        try {
            System.out.println("=== ProfileController.updateVehicle() CALLED ===");
            System.out.println("Vehicle data: " + vehicleData);
            return ResponseEntity.ok("{\"message\":\"Vehicle updated successfully!\"}");
        } catch (Exception e) {
            System.err.println("=== ProfileController.updateVehicle() ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * PUT /api/profile - Cập nhật profile
     */
    @PutMapping
    public ResponseEntity<String> updateProfile(@RequestBody String profileData) {
        try {
            System.out.println("=== ProfileController.updateProfile() CALLED ===");
            System.out.println("Profile data: " + profileData);
            return ResponseEntity.ok("{\"message\":\"Profile updated successfully!\"}");
        } catch (Exception e) {
            System.err.println("=== ProfileController.updateProfile() ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * PUT /api/profile/wallet - Cập nhật số dư ví
     */
    @PutMapping("/wallet")
    public ResponseEntity<String> updateWallet(@RequestBody UpdateWalletRequest request, Authentication auth) {
        try {
            System.out.println("=== ProfileController.updateWallet() CALLED ===");
            System.out.println("Amount to deduct: " + request.getAmount());
            
            // Lấy email từ JWT token
            String userEmail = auth.getName();
            System.out.println("Email from JWT: " + userEmail);
            
            // Tìm user trong database
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Kiểm tra số dư hiện tại
            Double currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
            System.out.println("Current balance: " + currentBalance);
            
            if (currentBalance < request.getAmount()) {
                return ResponseEntity.badRequest()
                    .body("{\"error\":\"Insufficient balance\",\"currentBalance\":" + currentBalance + "}");
            }
            
            // Trừ tiền từ ví
            Double newBalance = currentBalance - request.getAmount();
            user.setWalletBalance(newBalance);
            userRepository.save(user);
            
            System.out.println("New balance: " + newBalance);
            
            return ResponseEntity.ok("{\"message\":\"Wallet updated successfully\",\"newBalance\":" + newBalance + "}");
            
        } catch (Exception e) {
            System.err.println("=== ProfileController.updateWallet() ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    // DTO cho wallet update request
    public static class UpdateWalletRequest {
        private Double amount;
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}