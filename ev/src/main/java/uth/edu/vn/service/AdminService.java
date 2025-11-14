package uth.edu.vn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.entity.*;
import uth.edu.vn.enums.*;
import uth.edu.vn.exception.ResourceNotFoundException;
import uth.edu.vn.repository.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for Admin functionalities
 * REFACTORED: Sử dụng Spring Data JPA thay vì HibernateUtil
 * 
 * 1. Station & Charging Point Management
 * 2. User & Service Package Management  
 * 3. Reports & Analytics
 */
@Service
@Transactional
public class AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    @Autowired
    private TramSacRepository tramSacRepository;
    
    @Autowired
    private ChargerRepository chargerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PhienSacRepository phienSacRepository;
    
    @Autowired
    private GoiDichVuRepository goiDichVuRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // ==================== 1. STATION & CHARGING POINT MANAGEMENT ====================
    
    /**
     * Tạo trạm sạc mới
     */
    public TramSac createChargingStation(String stationName, String address, Double latitude, Double longitude, String operatingHours) {
        try {
            TramSac station = new TramSac(stationName, address, latitude, longitude);
            station = tramSacRepository.save(station);
            
            logger.info("Charging station created: {}", stationName);
            return station;
        } catch (Exception e) {
            logger.error("Error creating charging station: {}", stationName, e);
            throw new RuntimeException("Failed to create charging station", e);
        }
    }
    
    /**
     * Thêm charging point vào trạm
     */
    public Charger addChargingPoint(Long stationId, String pointName, ConnectorType connectorType, 
                                    Double powerCapacity, Double pricePerKwh) {
        TramSac station = tramSacRepository.findById(stationId)
            .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + stationId));
        
        Charger point = new Charger(pointName, connectorType, powerCapacity, pricePerKwh, station);
        point = chargerRepository.save(point);
        
        logger.info("Charging point added: {} to station {}", pointName, station.getName());
        return point;
    }
    
    /**
     * Lấy danh sách tất cả trạm sạc
     */
    @Transactional(readOnly = true)
    public List<TramSac> getAllStations() {
        List<TramSac> stations = tramSacRepository.findAll();
        logger.info("Total charging stations: {}", stations.size());
        return stations;
    }
    
    /**
     * Cập nhật trạng thái trạm sạc
     */
    public boolean updateStationStatus(Long stationId, StationStatus status) {
        try {
            TramSac station = tramSacRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + stationId));
            
            station.setStatus(status.name());
            tramSacRepository.save(station);
            
            logger.info("Station {} status updated to: {}", station.getName(), status);
            return true;
        } catch (Exception e) {
            logger.error("Error updating station status for id: {}", stationId, e);
            return false;
        }
    }
    
    /**
     * Cập nhật trạng thái charging point
     */
    public boolean updatePointStatus(Long pointId, PointStatus status) {
        try {
            Charger point = chargerRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging point not found with id: " + pointId));
            
            point.setStatus(status);
            chargerRepository.save(point);
            
            logger.info("Charging point {} status updated to: {}", point.getPointName(), status);
            return true;
        } catch (Exception e) {
            logger.error("Error updating charging point status for id: {}", pointId, e);
            return false;
        }
    }
    
    // ==================== 2. USER & SERVICE SUBSCRIPTION MANAGEMENT ====================
    
    /**
     * Tạo tài khoản CS Staff
     */
    public User createStaffAccount(String email, String password, String fullName, String phoneNumber) {
        // Check if email exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        // Split fullName into firstName and lastName
        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        // Create staff user with hashed password
        User staff = new User(email, passwordEncoder.encode(password), firstName, lastName, UserRole.CS_STAFF);
        staff.setPhone(phoneNumber);
        
        staff = userRepository.save(staff);
        logger.info("Staff account created: {}", email);
        
        return staff;
    }
    
    /**
     * Lấy danh sách tất cả users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        logger.info("Total users in system: {}", users.size());
        return users;
    }
    
    /**
     * Lấy users theo role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(UserRole role) {
        List<User> users = userRepository.findByRole(role);
        logger.info("Total {} users: {}", role, users.size());
        return users;
    }
    
    /**
     * Tạo subscription cho user
     */
    public GoiDichVu createUserSubscription(Long userId, String planName, Double monthlyFee) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user already has active subscription
        if (goiDichVuRepository.hasActiveSubscription(userId, LocalDateTime.now())) {
            throw new IllegalStateException("User already has an active subscription");
        }
        
        GoiDichVu subscription = new GoiDichVu(user, planName, java.math.BigDecimal.valueOf(monthlyFee));
        subscription = goiDichVuRepository.save(subscription);
        
        logger.info("Subscription created for user {}: {} (${}/month)", user.getEmail(), planName, monthlyFee);
        return subscription;
    }
    
    /**
     * Lấy tất cả active subscriptions
     */
    @Transactional(readOnly = true)
    public List<GoiDichVu> getActiveSubscriptions() {
        List<GoiDichVu> subscriptions = goiDichVuRepository.findAllActiveSubscriptions(LocalDateTime.now());
        logger.info("Total active subscriptions: {}", subscriptions.size());
        return subscriptions;
    }
    
    /**
     * Cancel subscription
     */
    public boolean cancelSubscription(Long subscriptionId) {
        try {
            GoiDichVu subscription = goiDichVuRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));
            
            subscription.cancelSubscription();
            goiDichVuRepository.save(subscription);
            
            logger.info("Subscription {} cancelled for user {}", subscriptionId, subscription.getUser().getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error cancelling subscription: {}", subscriptionId, e);
            return false;
        }
    }
    
    /**
     * Lấy subscription statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSubscriptionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Long activeCount = goiDichVuRepository.countActiveSubscriptions(LocalDateTime.now());
            java.math.BigDecimal monthlyRevenue = goiDichVuRepository.getTotalMonthlyRevenue(LocalDateTime.now());
            List<Object[]> planStats = goiDichVuRepository.getSubscriptionStatistics();
            
            stats.put("activeSubscriptions", activeCount);
            stats.put("monthlyRevenue", monthlyRevenue);
            stats.put("planStatistics", planStats);
            
            logger.info("=== SUBSCRIPTION STATISTICS ===");
            logger.info("Active Subscriptions: {}", activeCount);
            logger.info("Monthly Revenue: ${}", monthlyRevenue);
            logger.info("================================");
            
            return stats;
        } catch (Exception e) {
            logger.error("Error getting subscription statistics", e);
            return stats;
        }
    }
    
    // ==================== 3. REPORTS & ANALYTICS ====================
    
    /**
     * Tạo system overview
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // Total stations
            Long totalStations = tramSacRepository.count();
            
            // Total charging points
            Long totalPoints = chargerRepository.count();
            
            // Available points
            Long availablePoints = chargerRepository.countByStatus(PointStatus.AVAILABLE);
            
            // Total users
            Long totalUsers = userRepository.count();
            
            // Active sessions
            Long activeSessions = phienSacRepository.countByStatus(SessionStatus.ACTIVE);
            
            overview.put("totalStations", totalStations);
            overview.put("totalPoints", totalPoints);
            overview.put("availablePoints", availablePoints);
            overview.put("totalUsers", totalUsers);
            overview.put("activeSessions", activeSessions);
            
            logger.info("=== SYSTEM OVERVIEW ===");
            logger.info("Total Stations: {}", totalStations);
            logger.info("Total Charging Points: {}", totalPoints);
            logger.info("Available Points: {}", availablePoints);
            logger.info("Total Users: {}", totalUsers);
            logger.info("Active Sessions: {}", activeSessions);
            logger.info("=======================");
            
            return overview;
        } catch (Exception e) {
            logger.error("Error generating system overview", e);
            return overview;
        }
    }
    
    /**
     * Lấy revenue theo station
     */
    @Transactional(readOnly = true)
    public Map<Long, Double> getRevenueByStation() {
        Map<Long, Double> revenueByStation = new HashMap<>();
        
        try {
            List<TramSac> stations = tramSacRepository.findAll();
            
            logger.info("=== REVENUE BY STATION ===");
            for (TramSac station : stations) {
                Double revenue = phienSacRepository.getTotalRevenueByStation(station.getId());
                if (revenue != null && revenue > 0) {
                    revenueByStation.put(station.getId(), revenue);
                    logger.info("Station {} (ID {}): ${}", station.getName(), station.getId(), revenue);
                }
            }
            logger.info("===========================");
            
            return revenueByStation;
        } catch (Exception e) {
            logger.error("Error getting revenue by station", e);
            return revenueByStation;
        }
    }
    
    /**
     * Lấy usage statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUsageStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        try {
            // Count sessions by status
            for (SessionStatus status : SessionStatus.values()) {
                Long count = phienSacRepository.countByStatus(status);
                stats.put("sessions_" + status.toString().toLowerCase(), count);
            }
            
            logger.info("=== USAGE STATISTICS ===");
            stats.forEach((key, value) -> logger.info("{}: {}", key, value));
            logger.info("=========================");
            
            return stats;
        } catch (Exception e) {
            logger.error("Error getting usage statistics", e);
            return stats;
        }
    }
    
    /**
     * Tạo monthly report
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generateMonthlyReport(int year, int month) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get monthly statistics
            Long totalSessions = phienSacRepository.getMonthlySessionCount(year, month);
            Double totalEnergy = phienSacRepository.getMonthlyEnergyConsumed(year, month);
            Double totalRevenue = phienSacRepository.getMonthlyRevenue(year, month);
            
            report.put("year", year);
            report.put("month", month);
            report.put("totalSessions", totalSessions != null ? totalSessions : 0);
            report.put("totalEnergy", totalEnergy != null ? totalEnergy : 0.0);
            report.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
            report.put("avgSessionCost", totalSessions != null && totalSessions > 0 && totalRevenue != null 
                ? totalRevenue / totalSessions : 0.0);
            
            logger.info("=== MONTHLY REPORT {}/{} ===", month, year);
            logger.info("Total Sessions: {}", report.get("totalSessions"));
            logger.info("Total Energy Dispensed: {} kWh", report.get("totalEnergy"));
            logger.info("Total Revenue: ${}", report.get("totalRevenue"));
            logger.info("Average Session Cost: ${}", report.get("avgSessionCost"));
            logger.info("===============================================");
            
            return report;
        } catch (Exception e) {
            logger.error("Error generating monthly report for {}/{}", month, year, e);
            return report;
        }
    }
}