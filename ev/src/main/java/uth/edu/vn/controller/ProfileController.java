package uth.edu.vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.entity.User;
import uth.edu.vn.entity.Xe;
import uth.edu.vn.repository.UserRepository;
import uth.edu.vn.repository.XeRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Object>> getProfile(Authentication auth) {
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

            // Dữ liệu user
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            userData.put("phone", user.getPhone() != null ? user.getPhone() : "");
            userData.put("role", user.getRole() != null ? user.getRole().toString() : "");
            userData.put("walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO);

            // Dữ liệu vehicle (nếu có)
            Map<String, Object> vehicleData = null;
            if (userVehicle.isPresent()) {
                Xe vehicle = userVehicle.get();
                vehicleData = new HashMap<>();
                vehicleData.put("make", vehicle.getMake() != null ? vehicle.getMake() : "");
                vehicleData.put("model", vehicle.getModel() != null ? vehicle.getModel() : "");
                vehicleData.put("plateNumber", vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "");
                vehicleData.put("plugType", vehicle.getPlugType() != null ? vehicle.getPlugType() : "");
                // Alias cho frontend
                vehicleData.put("licensePlate", vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "");
                vehicleData.put("connectorType", vehicle.getPlugType() != null ? vehicle.getPlugType() : "");
                vehicleData.put("batteryCapacity", null);
            }

            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("user", userData);
            response.put("vehicle", vehicleData);

            // Alias các field phẳng cho JS cũ (hoTen, email, sdt, diaChi)
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            String fullName = (firstName + " " + lastName).trim();
            response.put("hoTen", fullName);
            response.put("email", user.getEmail());
            response.put("sdt", user.getPhone() != null ? user.getPhone() : "");
            response.put("diaChi", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("=== ProfileController.getProfile() ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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
    public ResponseEntity<Map<String, Object>> getVehicle(Authentication auth) {
        try {
            System.out.println("=== ProfileController.getVehicle() CALLED ===");

            String userEmail = auth.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<Xe> userVehicle = xeRepository.findFirstByUserId(user.getId());
            if (userVehicle.isEmpty()) {
                return ResponseEntity.status(404).body(null);
            }

            Xe vehicle = userVehicle.get();
            Map<String, Object> vehicleData = new HashMap<>();
            vehicleData.put("licensePlate", vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "");
            vehicleData.put("model", vehicle.getModel() != null ? vehicle.getModel() : "");
            vehicleData.put("connectorType", vehicle.getPlugType() != null ? vehicle.getPlugType() : "");
            vehicleData.put("batteryCapacity", null);

            return ResponseEntity.ok(vehicleData);
        } catch (Exception e) {
            System.err.println("=== ProfileController.getVehicle() ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * PUT /api/profile/vehicle - Cập nhật thông tin xe
     */
    @PostMapping("/vehicle")
    @PutMapping("/vehicle")
    public ResponseEntity<Map<String, Object>> saveVehicle(@RequestBody VehicleRequest request, Authentication auth) {
        try {
            System.out.println("=== ProfileController.saveVehicle() CALLED ===");

            String userEmail = auth.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (request.getLicensePlate() == null || request.getLicensePlate().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Biển số xe không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            Optional<Xe> existingVehicleByPlate = xeRepository.findByPlateNumber(request.getLicensePlate());
            if (existingVehicleByPlate.isPresent() && !existingVehicleByPlate.get().getUserId().equals(user.getId())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Biển số xe đã được đăng ký bởi người dùng khác");
                return ResponseEntity.badRequest().body(error);
            }

            Optional<Xe> userVehicle = xeRepository.findFirstByUserId(user.getId());
            Xe vehicle;
            if (userVehicle.isPresent()) {
                vehicle = userVehicle.get();
                vehicle.setPlateNumber(request.getLicensePlate());
                vehicle.setModel(request.getModel());
                vehicle.setPlugType(request.getConnectorType());
            } else {
                vehicle = new Xe();
                vehicle.setUserId(user.getId());
                vehicle.setPlateNumber(request.getLicensePlate());
                vehicle.setModel(request.getModel());
                vehicle.setPlugType(request.getConnectorType());
            }

            xeRepository.save(vehicle);

            Map<String, Object> vehicleData = new HashMap<>();
            vehicleData.put("licensePlate", vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "");
            vehicleData.put("model", vehicle.getModel() != null ? vehicle.getModel() : "");
            vehicleData.put("connectorType", vehicle.getPlugType() != null ? vehicle.getPlugType() : "");
            vehicleData.put("batteryCapacity", request.getBatteryCapacity());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vehicle saved successfully");
            response.put("vehicle", vehicleData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== ProfileController.saveVehicle() ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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
    public ResponseEntity<Map<String, Object>> updateWallet(@RequestBody UpdateWalletRequest request,
            Authentication auth) {
        try {
            System.out.println("=== ProfileController.updateWallet() CALLED ===");
            System.out.println("Amount to deduct: " + request.getAmount());

            String userEmail = auth.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            BigDecimal currentBalance = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
            System.out.println("Current balance: " + currentBalance);

            BigDecimal amountToDeduct = BigDecimal.valueOf(request.getAmount());
            if (currentBalance.compareTo(amountToDeduct) < 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Insufficient balance");
                error.put("currentBalance", currentBalance);
                return ResponseEntity.badRequest().body(error);
            }

            BigDecimal newBalance = currentBalance.subtract(amountToDeduct);
            user.setWalletBalance(newBalance);
            userRepository.save(user);

            System.out.println("New balance: " + newBalance);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Wallet updated successfully");
            response.put("newBalance", newBalance);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("=== ProfileController.updateWallet() ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/wallet")
    public ResponseEntity<Map<String, Object>> getWallet(Authentication auth) {
        try {
            String userEmail = auth.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            BigDecimal balance = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
            Map<String, Object> response = new HashMap<>();
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    public static class VehicleRequest {
        private String licensePlate;
        private String model;
        private String connectorType;
        private Double batteryCapacity;

        public String getLicensePlate() {
            return licensePlate;
        }

        public void setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getConnectorType() {
            return connectorType;
        }

        public void setConnectorType(String connectorType) {
            this.connectorType = connectorType;
        }

        public Double getBatteryCapacity() {
            return batteryCapacity;
        }

        public void setBatteryCapacity(Double batteryCapacity) {
            this.batteryCapacity = batteryCapacity;
        }
    }

    public static class UpdateWalletRequest {
        private Double amount;

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }
    }
}