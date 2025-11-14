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
import java.util.List;

/**
 * Service class for Charging Station Staff functionalities
 * REFACTORED: Sử dụng Spring Data JPA thay vì HibernateUtil
 * 
 * 1. Payment management at charging stations
 * 2. Monitoring and reporting
 */
@Service
@Transactional
public class CSStaffService {
    
    private static final Logger logger = LoggerFactory.getLogger(CSStaffService.class);
    
    @Autowired
    private ChargerRepository chargerRepository;
    
    @Autowired
    private PhienSacRepository phienSacRepository;
    
    @Autowired
    private ThanhToanRepository thanhToanRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private XeRepository xeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // ==================== 1. PAYMENT MANAGEMENT AT CHARGING STATIONS ====================
    
    /**
     * Start charging session by staff for walk-in customers
     * Creates temporary user and vehicle for cash payments
     */
    public PhienSac startSessionByStaff(Long pointId, String vehiclePlate) {
        try {
            Charger point = chargerRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging point not found with id: " + pointId));
            
            if (point.getStatus() != PointStatus.AVAILABLE) {
                logger.warn("Charging point {} is not available. Status: {}", pointId, point.getStatus());
                return null;
            }
            
            // Create temporary user for walk-in customers
            User walkInUser = new User(
                "walkin_" + System.currentTimeMillis() + "@temp.com", 
                passwordEncoder.encode("temp123"), 
                "Walk-in", 
                "Customer", 
                UserRole.EV_DRIVER
            );
            walkInUser = userRepository.save(walkInUser);
            
            // Create vehicle for walk-in customer
            Xe walkInVehicle = new Xe();
            walkInVehicle.setUserId(walkInUser.getId());
            walkInVehicle.setPlateNumber(vehiclePlate);
            walkInVehicle.setPlugType("Type2"); // Default plug type
            walkInVehicle = xeRepository.save(walkInVehicle);
            
            // Create charging session
            PhienSac chargingSession = new PhienSac(walkInUser, point, "STAFF_" + System.currentTimeMillis());
            chargingSession = phienSacRepository.save(chargingSession);
            
            // Update charging point status
            point.setStatus(PointStatus.OCCUPIED);
            chargerRepository.save(point);
            
            logger.info("Charging session started by staff for vehicle: {}", vehiclePlate);
            return chargingSession;
            
        } catch (Exception e) {
            logger.error("Error starting charging session by staff for vehicle: {}", vehiclePlate, e);
            return null;
        }
    }
    
    /**
     * Stop charging session and calculate final cost
     */
    public boolean stopChargingSession(Long sessionId, Double energyConsumed, Integer endSoc) {
        try {
            PhienSac chargingSession = phienSacRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with id: " + sessionId));
            
            if (chargingSession.getStatus() != SessionStatus.ACTIVE) {
                logger.warn("Cannot stop session {}. Current status: {}", sessionId, chargingSession.getStatus());
                return false;
            }
            
            // Update session details
            chargingSession.setEndTime(LocalDateTime.now());
            chargingSession.setEnergyConsumed(energyConsumed);
            chargingSession.setEndSoc(endSoc);
            chargingSession.setStatus(SessionStatus.COMPLETED);
            
            // Calculate total cost
            Double pricePerKwh = chargingSession.getChargingPoint().getPricePerKwh();
            Double totalCost = energyConsumed * pricePerKwh;
            chargingSession.setTotalCost(totalCost);
            
            phienSacRepository.save(chargingSession);
            
            // Update charging point status
            Charger point = chargingSession.getChargingPoint();
            point.setStatus(PointStatus.AVAILABLE);
            chargerRepository.save(point);
            
            logger.info("Charging session stopped. Energy: {} kWh, Cost: ${}", energyConsumed, totalCost);
            return true;
            
        } catch (Exception e) {
            logger.error("Error stopping charging session: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * Process cash payment for completed charging session
     */
    public ThanhToan processCashPayment(Long sessionId) {
        try {
            PhienSac chargingSession = phienSacRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with id: " + sessionId));
            
            if (chargingSession.getTotalCost() == null) {
                logger.warn("Cannot process payment - session {} has no total cost", sessionId);
                return null;
            }
            
            ThanhToan payment = new ThanhToan(
                chargingSession.getSessionId(), 
                java.math.BigDecimal.valueOf(chargingSession.getTotalCost()), 
                "CASH"
            );
            payment.setStatus("COMPLETED");
            
            payment = thanhToanRepository.save(payment);
            
            logger.info("Cash payment processed: ${}", chargingSession.getTotalCost());
            return payment;
            
        } catch (Exception e) {
            logger.error("Error processing cash payment for session: {}", sessionId, e);
            return null;
        }
    }
    
    // ==================== 2. MONITORING AND REPORTING ====================
    
    /**
     * Get status of all charging points at a station
     */
    @Transactional(readOnly = true)
    public List<Charger> getStationStatus(Long stationId) {
        try {
            List<Charger> points = chargerRepository.findByChargingStationId(stationId);
            
            logger.info("=== STATION STATUS REPORT ===");
            for (Charger point : points) {
                logger.info("Point {}: {} ({} kW, {})", 
                    point.getPointName(), point.getStatus(), 
                    point.getPowerCapacity(), point.getConnectorType());
            }
            logger.info("=============================");
            
            return points;
            
        } catch (Exception e) {
            logger.error("Error getting station status for station: {}", stationId, e);
            return null;
        }
    }
    
    /**
     * Get all active charging sessions at a station
     */
    @Transactional(readOnly = true)
    public List<PhienSac> getActiveSessionsAtStation(Long stationId) {
        try {
            List<PhienSac> activeSessions = phienSacRepository
                .findByChargingPointChargingStationIdAndStatus(stationId, SessionStatus.ACTIVE);
            
            logger.info("Active sessions at station {}: {}", stationId, activeSessions.size());
            return activeSessions;
            
        } catch (Exception e) {
            logger.error("Error getting active sessions for station: {}", stationId, e);
            return null;
        }
    }
    
    /**
     * Report incident and mark charging point as out of order
     */
    public boolean reportIncident(Long stationId, Long pointId, String description) {
        try {
            Charger point = chargerRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging point not found with id: " + pointId));
            
            if (!point.getChargingStation().getId().equals(stationId)) {
                logger.warn("Point {} does not belong to station {}", pointId, stationId);
                return false;
            }
            
            // Mark point as out of order
            point.setStatus(PointStatus.OUT_OF_ORDER);
            chargerRepository.save(point);
            
            logger.info("Incident reported for point {}: {}", point.getPointName(), description);
            logger.info("Point marked as OUT_OF_ORDER");
            return true;
            
        } catch (Exception e) {
            logger.error("Error reporting incident for point: {}", pointId, e);
            return false;
        }
    }
    
    /**
     * Generate daily report for a station
     */
    @Transactional(readOnly = true)
    public void generateDailyReport(Long stationId) {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            
            List<PhienSac> todaySessions = phienSacRepository
                .findByChargingPointChargingStationIdAndStartTimeBetween(stationId, startOfDay, endOfDay);
            
            Double totalRevenue = 0.0;
            Double totalEnergy = 0.0;
            
            for (PhienSac cs : todaySessions) {
                if (cs.getTotalCost() != null) {
                    totalRevenue += cs.getTotalCost();
                }
                if (cs.getEnergyConsumed() != null) {
                    totalEnergy += cs.getEnergyConsumed();
                }
            }
            
            logger.info("=== DAILY REPORT ===");
            logger.info("Date: {}", LocalDateTime.now().toLocalDate());
            logger.info("Total Sessions: {}", todaySessions.size());
            logger.info("Total Energy Dispensed: {} kWh", totalEnergy);
            logger.info("Total Revenue: ${}", totalRevenue);
            logger.info("====================");
            
        } catch (Exception e) {
            logger.error("Error generating daily report for station: {}", stationId, e);
        }
    }
}