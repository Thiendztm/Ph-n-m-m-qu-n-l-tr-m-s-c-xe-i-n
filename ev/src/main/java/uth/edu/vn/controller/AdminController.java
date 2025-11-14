package uth.edu.vn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uth.edu.vn.entity.*;
import uth.edu.vn.enums.*;
import uth.edu.vn.service.AdminService;
import uth.edu.vn.repository.*;

import java.util.*;
import java.time.LocalDateTime;

/**
 * Admin Controller
 * REST API endpoints cho quản trị viên
 * Yêu cầu role ADMIN để truy cập
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private TramSacRepository tramSacRepository;
    
    @Autowired
    private ChargerRepository chargerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PhienSacRepository phienSacRepository;
    
    // ==================== STATION MANAGEMENT ====================
    
    /**
     * Lấy tất cả trạm sạc
     * GET /api/admin/stations
     */
    @GetMapping("/stations")
    public ResponseEntity<Map<String, Object>> getAllStations(
            @RequestParam(required = false) String status) {
        try {
            List<TramSac> stations;
            
            if (status != null && !status.isEmpty()) {
                StationStatus stationStatus = StationStatus.valueOf(status.toUpperCase());
                stations = tramSacRepository.findByStatus(stationStatus);
            } else {
                stations = adminService.getAllStations();
            }
            
            List<Map<String, Object>> stationList = new ArrayList<>();
            for (TramSac station : stations) {
                Map<String, Object> stationData = new HashMap<>();
                stationData.put("id", station.getId());
                stationData.put("name", station.getName());
                stationData.put("address", station.getAddress());
                stationData.put("latitude", station.getLatitude());
                stationData.put("longitude", station.getLongitude());
                stationData.put("status", station.getStatus());
                
                // Đếm số điểm sạc
                List<Charger> chargers = chargerRepository.findByChargingStationId(station.getId());
                long availableCount = chargers.stream()
                    .filter(c -> c.getStatus() == PointStatus.AVAILABLE)
                    .count();
                
                stationData.put("totalChargers", chargers.size());
                stationData.put("availableChargers", availableCount);
                
                stationList.add(stationData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stations", stationList);
            response.put("total", stationList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi lấy danh sách trạm sạc: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Tạo trạm sạc mới
     * POST /api/admin/stations
     */
    @PostMapping("/stations")
    public ResponseEntity<Map<String, Object>> createStation(
            @RequestBody Map<String, Object> stationData) {
        try {
            String name = (String) stationData.get("name");
            String address = (String) stationData.get("address");
            Double latitude = Double.parseDouble(stationData.get("latitude").toString());
            Double longitude = Double.parseDouble(stationData.get("longitude").toString());
            String operatingHours = (String) stationData.getOrDefault("operatingHours", "24/7");
            
            TramSac newStation = adminService.createChargingStation(
                name, address, latitude, longitude, operatingHours
            );
            
            Map<String, Object> response = new HashMap<>();
            if (newStation != null) {
                response.put("success", true);
                response.put("message", "Tạo trạm sạc thành công");
                response.put("stationId", newStation.getId());
                response.put("station", Map.of(
                    "id", newStation.getId(),
                    "name", newStation.getName(),
                    "address", newStation.getAddress(),
                    "status", newStation.getStatus()
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Không thể tạo trạm sạc");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi tạo trạm sạc: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Cập nhật trạng thái trạm sạc
     * PUT /api/admin/stations/{stationId}/status
     */
    @PutMapping("/stations/{stationId}/status")
    public ResponseEntity<Map<String, Object>> updateStationStatus(
            @PathVariable Long stationId,
            @RequestBody Map<String, String> statusData) {
        try {
            String statusStr = statusData.get("status");
            StationStatus newStatus = StationStatus.valueOf(statusStr.toUpperCase());
            
            boolean success = adminService.updateStationStatus(stationId, newStatus);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Cập nhật trạng thái thành công");
                response.put("newStatus", newStatus);
            } else {
                response.put("success", false);
                response.put("error", "Không thể cập nhật trạng thái");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Thêm điểm sạc vào trạm
     * POST /api/admin/stations/{stationId}/chargers
     */
    @PostMapping("/stations/{stationId}/chargers")
    public ResponseEntity<Map<String, Object>> addChargingPoint(
            @PathVariable Long stationId,
            @RequestBody Map<String, Object> chargerData) {
        try {
            String pointName = (String) chargerData.get("pointName");
            ConnectorType connectorType = ConnectorType.valueOf(
                ((String) chargerData.get("connectorType")).toUpperCase()
            );
            Double powerCapacity = Double.parseDouble(chargerData.get("powerCapacity").toString());
            Double pricePerKwh = Double.parseDouble(chargerData.get("pricePerKwh").toString());
            
            Charger newCharger = adminService.addChargingPoint(
                stationId, pointName, connectorType, powerCapacity, pricePerKwh
            );
            
            Map<String, Object> response = new HashMap<>();
            if (newCharger != null) {
                response.put("success", true);
                response.put("message", "Thêm điểm sạc thành công");
                response.put("chargerId", newCharger.getPointId());
                response.put("charger", Map.of(
                    "id", newCharger.getPointId(),
                    "name", newCharger.getPointName(),
                    "connectorType", newCharger.getConnectorType(),
                    "powerCapacity", newCharger.getPowerCapacity(),
                    "status", newCharger.getStatus()
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Không thể thêm điểm sạc");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi thêm điểm sạc: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Lấy tất cả người dùng
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(required = false) String role) {
        try {
            List<User> users;
            
            if (role != null && !role.isEmpty()) {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                users = adminService.getUsersByRole(userRole);
            } else {
                users = adminService.getAllUsers();
            }
            
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("email", user.getEmail());
                userData.put("firstName", user.getFirstName());
                userData.put("lastName", user.getLastName());
                userData.put("phone", user.getPhone());
                userData.put("role", user.getRole());
                userData.put("walletBalance", user.getWalletBalance());
                userData.put("createdAt", user.getCreatedAt());
                
                userList.add(userData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", userList);
            response.put("total", userList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi lấy danh sách người dùng: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Tạo tài khoản nhân viên
     * POST /api/admin/staff
     */
    @PostMapping("/staff")
    public ResponseEntity<Map<String, Object>> createStaffAccount(
            @RequestBody Map<String, String> staffData) {
        try {
            String email = staffData.get("email");
            String password = staffData.get("password");
            String fullName = staffData.get("fullName");
            String phoneNumber = staffData.get("phoneNumber");
            
            User newStaff = adminService.createStaffAccount(
                email, password, fullName, phoneNumber
            );
            
            Map<String, Object> response = new HashMap<>();
            if (newStaff != null) {
                response.put("success", true);
                response.put("message", "Tạo tài khoản nhân viên thành công");
                response.put("userId", newStaff.getId());
                response.put("user", Map.of(
                    "id", newStaff.getId(),
                    "email", newStaff.getEmail(),
                    "fullName", newStaff.getFirstName() + " " + newStaff.getLastName(),
                    "role", newStaff.getRole()
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Không thể tạo tài khoản nhân viên");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi tạo tài khoản: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // ==================== REPORTS & STATISTICS ====================
    
    /**
     * Tổng quan hệ thống
     * GET /api/admin/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        try {
            // Đếm stations theo status
            long totalStations = tramSacRepository.count();
            long onlineStations = tramSacRepository.findByStatus(StationStatus.ONLINE).size();
            long offlineStations = tramSacRepository.findByStatus(StationStatus.OFFLINE).size();
            
            // Đếm chargers theo status
            long totalChargers = chargerRepository.count();
            Long availableChargers = chargerRepository.countByStatus(PointStatus.AVAILABLE);
            Long occupiedChargers = chargerRepository.countByStatus(PointStatus.OCCUPIED);
            Long outOfOrderChargers = chargerRepository.countByStatus(PointStatus.OUT_OF_ORDER);
            
            // Đếm users theo role
            long totalUsers = userRepository.count();
            long drivers = adminService.getUsersByRole(UserRole.EV_DRIVER).size();
            long staff = adminService.getUsersByRole(UserRole.CS_STAFF).size();
            long admins = adminService.getUsersByRole(UserRole.ADMIN).size();
            
            // Đếm sessions theo status
            Long activeSessions = phienSacRepository.countByStatus(SessionStatus.ACTIVE);
            Long completedSessions = phienSacRepository.countByStatus(SessionStatus.COMPLETED);
            
            Map<String, Object> overview = new HashMap<>();
            
            // Stations
            Map<String, Object> stationsData = new HashMap<>();
            stationsData.put("total", totalStations);
            stationsData.put("online", onlineStations);
            stationsData.put("offline", offlineStations);
            stationsData.put("maintenance", 0);
            overview.put("stations", stationsData);
            
            // Chargers
            Map<String, Object> chargersData = new HashMap<>();
            chargersData.put("total", totalChargers);
            chargersData.put("available", availableChargers != null ? availableChargers : 0);
            chargersData.put("occupied", occupiedChargers != null ? occupiedChargers : 0);
            chargersData.put("outOfOrder", outOfOrderChargers != null ? outOfOrderChargers : 0);
            overview.put("chargers", chargersData);
            
            // Users
            Map<String, Object> usersData = new HashMap<>();
            usersData.put("total", totalUsers);
            usersData.put("drivers", drivers);
            usersData.put("staff", staff);
            usersData.put("admins", admins);
            overview.put("users", usersData);
            
            // Sessions
            Map<String, Object> sessionsData = new HashMap<>();
            sessionsData.put("active", activeSessions != null ? activeSessions : 0);
            sessionsData.put("completed", completedSessions != null ? completedSessions : 0);
            overview.put("sessions", sessionsData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("overview", overview);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi lấy tổng quan hệ thống: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Doanh thu theo trạm
     * GET /api/admin/revenue
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueByStation() {
        try {
            List<TramSac> stations = tramSacRepository.findAll();
            List<Map<String, Object>> revenueList = new ArrayList<>();
            double totalRevenue = 0.0;
            
            for (TramSac station : stations) {
                Double stationRevenue = phienSacRepository.getTotalRevenueByStation(station.getId());
                if (stationRevenue == null) stationRevenue = 0.0;
                
                Map<String, Object> revenueData = new HashMap<>();
                revenueData.put("stationId", station.getId());
                revenueData.put("stationName", station.getName());
                revenueData.put("revenue", stationRevenue);
                
                revenueList.add(revenueData);
                totalRevenue += stationRevenue;
            }
            
            // Sắp xếp theo doanh thu giảm dần
            revenueList.sort((a, b) -> 
                Double.compare((Double) b.get("revenue"), (Double) a.get("revenue"))
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("revenues", revenueList);
            response.put("totalRevenue", totalRevenue);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi lấy doanh thu: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Báo cáo theo tháng
     * GET /api/admin/reports/monthly
     */
    @GetMapping("/reports/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            // Mặc định lấy tháng hiện tại
            LocalDateTime now = LocalDateTime.now();
            if (year == null) year = now.getYear();
            if (month == null) month = now.getMonthValue();
            
            // Lấy dữ liệu từ repository
            Long sessionCount = phienSacRepository.getMonthlySessionCount(year, month);
            Double energyConsumed = phienSacRepository.getMonthlyEnergyConsumed(year, month);
            Double revenue = phienSacRepository.getMonthlyRevenue(year, month);
            
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("year", year);
            reportData.put("month", month);
            reportData.put("totalSessions", sessionCount != null ? sessionCount : 0);
            reportData.put("totalEnergyConsumed", energyConsumed != null ? energyConsumed : 0.0);
            reportData.put("totalRevenue", revenue != null ? revenue : 0.0);
            
            // Tính trung bình
            long sessions = sessionCount != null ? sessionCount : 0;
            if (sessions > 0) {
                reportData.put("avgEnergyPerSession", energyConsumed / sessions);
                reportData.put("avgRevenuePerSession", revenue / sessions);
            } else {
                reportData.put("avgEnergyPerSession", 0.0);
                reportData.put("avgRevenuePerSession", 0.0);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", reportData);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi tạo báo cáo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Thống kê sử dụng
     * GET /api/admin/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUsageStatistics() {
        try {
            // Tổng số sessions
            long totalSessions = phienSacRepository.count();
            Long activeSessions = phienSacRepository.countByStatus(SessionStatus.ACTIVE);
            Long completedSessions = phienSacRepository.countByStatus(SessionStatus.COMPLETED);
            
            // Top stations (theo số lượng sessions)
            List<TramSac> stations = tramSacRepository.findAll();
            List<Map<String, Object>> topStations = new ArrayList<>();
            
            for (TramSac station : stations) {
                List<Charger> chargers = chargerRepository.findByChargingStationId(station.getId());
                long sessionCount = 0;
                
                for (Charger charger : chargers) {
                    sessionCount += phienSacRepository.count();
                }
                
                if (sessionCount > 0) {
                    Map<String, Object> stationData = new HashMap<>();
                    stationData.put("stationId", station.getId());
                    stationData.put("stationName", station.getName());
                    stationData.put("sessionCount", sessionCount);
                    topStations.add(stationData);
                }
            }
            
            // Sắp xếp theo số lượng sessions
            topStations.sort((a, b) -> 
                Long.compare((Long) b.get("sessionCount"), (Long) a.get("sessionCount"))
            );
            
            // Lấy top 5
            if (topStations.size() > 5) {
                topStations = topStations.subList(0, 5);
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalSessions", totalSessions);
            statistics.put("activeSessions", activeSessions != null ? activeSessions : 0);
            statistics.put("completedSessions", completedSessions != null ? completedSessions : 0);
            statistics.put("topStations", topStations);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Lỗi khi lấy thống kê: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
