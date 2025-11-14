package uth.edu.vn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.entity.*;
import uth.edu.vn.enums.*;
import uth.edu.vn.exception.BadRequestException;
import uth.edu.vn.exception.ResourceNotFoundException;
import uth.edu.vn.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service class for EV Driver functionalities
 * REFACTORED: Sử dụng Spring Data JPA thay vì HibernateUtil
 * 
 * 1. Registration & Account Management
 * 2. Booking & Starting charging sessions
 * 3. Payment & E-wallet
 * 4. History & Personal analytics
 */
@Service
@Transactional
public class EVDriverService {
    
    private static final Logger logger = LoggerFactory.getLogger(EVDriverService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TramSacRepository tramSacRepository;
    
    @Autowired
    private ChargerRepository chargerRepository;
    
    @Autowired
    private PhienSacRepository phienSacRepository;
    
    @Autowired
    private DatChoRepository datChoRepository;
    
    @Autowired
    private ThanhToanRepository thanhToanRepository;
    
    @Autowired
    private XeRepository xeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // ==================== 1. REGISTRATION & ACCOUNT MANAGEMENT ====================
    
    /**
     * Register new EV driver
     * Note: This is for backward compatibility. AuthService should be used for new registrations.
     */
    public User registerDriver(String email, String password, String fullName, String phoneNumber) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                logger.warn("Email already exists: {}", email);
                throw new BadRequestException("Email already exists: " + email);
            }
            
            // Split fullName into firstName and lastName
            String[] nameParts = fullName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            // Create driver with encoded password
            User driver = new User(email, passwordEncoder.encode(password), firstName, lastName, UserRole.EV_DRIVER);
            driver.setPhone(phoneNumber);
            
            driver = userRepository.save(driver);
            
            logger.info("EV Driver registered successfully: {}", email);
            return driver;
            
        } catch (Exception e) {
            logger.error("Error registering driver: {}", email, e);
            return null;
        }
    }
    
    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            return null;
        }
    }
    
    /**
     * Save or update user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Login method (backward compatibility - AuthService should be used)
     * @deprecated Use AuthService.login() instead
     */
    @Deprecated
    @Transactional(readOnly = true)
    public User login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user != null && user.getRole() == UserRole.EV_DRIVER) {
                // In real app, should use passwordEncoder.matches()
                // For now, just check if user exists
                if (passwordEncoder.matches(password, user.getPassword())) {
                    logger.info("Login successful for: {}", email);
                    return user;
                }
            }
            
            logger.warn("Invalid credentials for: {}", email);
            return null;
            
        } catch (Exception e) {
            logger.error("Error during login for: {}", email, e);
            return null;
        }
    }
    
    /**
     * Update driver profile with vehicle information
     */
    public boolean updateProfile(Long userId, String vehicleModel, String vehiclePlate, Double batteryCapacity) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
            if (user.getRole() != UserRole.EV_DRIVER) {
                logger.warn("User {} is not an EV driver", userId);
                return false;
            }
            
            // Check if vehicle already exists
            Xe vehicle = xeRepository.findByPlateNumber(vehiclePlate).orElse(null);
            
            if (vehicle == null) {
                // Create new vehicle
                String[] modelParts = vehicleModel.split(" ", 2);
                String make = modelParts.length > 0 ? modelParts[0] : "Unknown";
                String model = modelParts.length > 1 ? modelParts[1] : vehicleModel;
                
                vehicle = new Xe(userId, make, model, vehiclePlate, "Type2");
            } else {
                // Update existing vehicle
                vehicle.setModel(vehicleModel);
                vehicle.setUserId(userId);
            }
            
            xeRepository.save(vehicle);
            
            logger.info("Profile updated for user: {}", user.getEmail());
            return true;
            
        } catch (Exception e) {
            logger.error("Error updating profile for user: {}", userId, e);
            return false;
        }
    }
    
    // ==================== 2. BOOKING & STARTING CHARGING SESSIONS ====================
    
    /**
     * Find nearby charging stations
     * In production, should use spatial queries with actual distance calculation
     */
    @Transactional(readOnly = true)
    public List<TramSac> findNearbyStations(Double latitude, Double longitude, Double radiusKm) {
        try {
            // Simple implementation: return all online stations
            // TODO: Implement actual spatial distance calculation
            List<TramSac> stations = tramSacRepository.findByStatus(StationStatus.ONLINE);
            
            logger.info("Found {} nearby charging stations", stations.size());
            return stations;
            
        } catch (Exception e) {
            logger.error("Error finding nearby stations", e);
            return null;
        }
    }
    
    /**
     * Get available charging points at a station
     */
    @Transactional(readOnly = true)
    public List<Charger> getAvailablePoints(Long stationId) {
        try {
            List<Charger> points = chargerRepository.findByChargingStationIdAndStatus(stationId, PointStatus.AVAILABLE);
            
            logger.info("Found {} available charging points at station {}", points.size(), stationId);
            return points;
            
        } catch (Exception e) {
            logger.error("Error getting available points for station: {}", stationId, e);
            return null;
        }
    }
    
    /**
     * Create booking for charging point
     */
    public DatCho createBooking(Long userId, Long pointId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
            Charger point = chargerRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging point not found with id: " + pointId));
            
            if (point.getStatus() != PointStatus.AVAILABLE) {
                logger.warn("Charging point {} is not available. Status: {}", pointId, point.getStatus());
                return null;
            }
            
            // Check for conflicting bookings
            List<DatCho> conflicts = datChoRepository.findConflictingBookings(pointId, startTime, endTime);
            if (!conflicts.isEmpty()) {
                logger.warn("Conflicting bookings found for point {} at {}", pointId, startTime);
                return null;
            }
            
            // Create booking
            DatCho booking = new DatCho(user, point, startTime, endTime);
            booking = datChoRepository.save(booking);
            
            // Reserve the charging point
            point.setStatus(PointStatus.RESERVED);
            chargerRepository.save(point);
            
            logger.info("Booking created successfully for user: {}", user.getEmail());
            return booking;
            
        } catch (Exception e) {
            logger.error("Error creating booking for user {} at point {}", userId, pointId, e);
            return null;
        }
    }
    
    /**
     * Start charging session
     */
    public PhienSac startChargingSession(Long userId, Long pointId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
            Charger point = chargerRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging point not found with id: " + pointId));
            
            if (point.getStatus() != PointStatus.AVAILABLE && point.getStatus() != PointStatus.RESERVED) {
                logger.warn("Cannot start session - point {} status: {}", pointId, point.getStatus());
                return null;
            }
            
            // Generate unique QR code
            String qrCode = UUID.randomUUID().toString();
            PhienSac chargingSession = new PhienSac(user, point, qrCode);
            chargingSession = phienSacRepository.save(chargingSession);
            
            // Update charging point status
            point.setStatus(PointStatus.OCCUPIED);
            chargerRepository.save(point);
            
            logger.info("Charging session started with QR Code: {}", qrCode);
            return chargingSession;
            
        } catch (Exception e) {
            logger.error("Error starting charging session for user {} at point {}", userId, pointId, e);
            return null;
        }
    }
    
    // ==================== 3. PAYMENT & E-WALLET ====================
    
    /**
     * Add funds to user's wallet
     */
    public boolean addFundsToWallet(Long userId, Double amount) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
            user.addFunds(BigDecimal.valueOf(amount));
            userRepository.save(user);
            
            logger.info("Added ${} to wallet. New balance: ${}", amount, user.getWalletBalance());
            return true;
            
        } catch (Exception e) {
            logger.error("Error adding funds to wallet for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * Process payment for charging session
     */
    public ThanhToan processPayment(Long sessionId, PaymentMethod paymentMethod) {
        try {
            PhienSac chargingSession = phienSacRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with id: " + sessionId));
            
            if (chargingSession.getTotalCost() == null) {
                logger.warn("Cannot process payment - session {} has no total cost", sessionId);
                return null;
            }
            
            User user = chargingSession.getUser();
            Double amount = chargingSession.getTotalCost();
            
            // Check wallet balance if using wallet payment
            if (paymentMethod == PaymentMethod.WALLET) {
                if (user.getWalletBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
                    logger.warn("Insufficient wallet balance for user: {}", user.getId());
                    throw new BadRequestException("Insufficient wallet balance");
                }
                
                // Deduct from wallet
                user.setWalletBalance(user.getWalletBalance().subtract(BigDecimal.valueOf(amount)));
                userRepository.save(user);
            }
            
            // Create payment record
            ThanhToan payment = new ThanhToan(
                chargingSession.getSessionId(), 
                BigDecimal.valueOf(amount), 
                paymentMethod.name()
            );
            payment.setStatus("COMPLETED");
            payment = thanhToanRepository.save(payment);
            
            logger.info("Payment processed successfully: ${}", amount);
            return payment;
            
        } catch (Exception e) {
            logger.error("Error processing payment for session: {}", sessionId, e);
            return null;
        }
    }
    
    // ==================== 4. HISTORY & PERSONAL ANALYTICS ====================
    
    /**
     * Get user's charging history
     */
    @Transactional(readOnly = true)
    public List<PhienSac> getChargingHistory(Long userId) {
        try {
            List<PhienSac> sessions = phienSacRepository.findByUserOrderByStartTimeDesc(userId);
            
            logger.info("Found {} charging sessions in history for user {}", sessions.size(), userId);
            return sessions;
            
        } catch (Exception e) {
            logger.error("Error getting charging history for user: {}", userId, e);
            return null;
        }
    }
    
    /**
     * Get monthly charging cost for user
     */
    @Transactional(readOnly = true)
    public Double getMonthlyChargingCost(Long userId, int year, int month) {
        try {
            Double totalCost = phienSacRepository.getMonthlyChargingCost(userId, month, year);
            totalCost = totalCost != null ? totalCost : 0.0;
            
            logger.info("Monthly charging cost for {}/{}: ${}", month, year, totalCost);
            return totalCost;
            
        } catch (Exception e) {
            logger.error("Error getting monthly charging cost for user: {}", userId, e);
            return 0.0;
        }
    }
}