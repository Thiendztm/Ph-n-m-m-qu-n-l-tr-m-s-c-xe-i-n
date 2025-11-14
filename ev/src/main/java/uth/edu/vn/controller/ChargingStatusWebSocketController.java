package uth.edu.vn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import uth.edu.vn.dto.ChargingStatusUpdate;
import uth.edu.vn.entity.PhienSac;
import uth.edu.vn.enums.SessionStatus;
import uth.edu.vn.repository.PhienSacRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * WebSocket Controller for Real-time Charging Status Updates
 * 
 * Broadcasts charging status to subscribed clients every 5 seconds
 * 
 * Client subscribes to: /topic/charging/{sessionId}
 * Receives: ChargingStatusUpdate JSON
 */
@Controller
@EnableScheduling
public class ChargingStatusWebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChargingStatusWebSocketController.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private PhienSacRepository phienSacRepository;
    
    private final Random random = new Random();
    
    /**
     * Scheduled task to broadcast charging status updates
     * Runs every 5 seconds for all active charging sessions
     */
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void broadcastChargingStatus() {
        // Get all active charging sessions
        List<PhienSac> activeSessions = phienSacRepository.findByStatus(SessionStatus.ACTIVE);
        
        for (PhienSac session : activeSessions) {
            try {
                ChargingStatusUpdate update = buildStatusUpdate(session);
                
                // Broadcast to topic for this specific session
                messagingTemplate.convertAndSend(
                    "/topic/charging/" + session.getSessionId(), 
                    update
                );
                
                logger.debug("Broadcasted status for session {}: SOC={}%, Cost={}đ", 
                    session.getSessionId(), update.getStateOfCharge(), update.getCurrentCost());
                
                // Check if charging complete (SOC >= 100% or target reached)
                if (update.getStateOfCharge() >= 100.0) {
                    update.setStatus("COMPLETED");
                    update.setAlertMessage("Sạc đầy! Vui lòng ngắt kết nối.");
                    
                    // Send completion notification
                    messagingTemplate.convertAndSend(
                        "/topic/charging/" + session.getSessionId(), 
                        update
                    );
                    
                    // Update session status in database
                    session.setStatus(SessionStatus.COMPLETED);
                    session.setEndTime(LocalDateTime.now());
                    phienSacRepository.save(session);
                    
                    logger.info("Session {} completed - 100% charged", session.getSessionId());
                }
                
            } catch (Exception e) {
                logger.error("Error broadcasting status for session {}", session.getSessionId(), e);
            }
        }
    }
    
    /**
     * Build charging status update from session
     */
    private ChargingStatusUpdate buildStatusUpdate(PhienSac session) {
        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(session.getStartTime(), now);
        long minutesElapsed = elapsed.toMinutes();
        
        // Simulate charging progress based on time
        // Real implementation would get data from charger hardware
        double socPerMinute = 2.0; // 2% per minute (typical for fast charging)
        double stateOfCharge = Math.min(100.0, minutesElapsed * socPerMinute);
        
        // Calculate energy consumed (kWh)
        // Assuming average 50kW charger
        double powerOutput = session.getChargingPoint().getPowerCapacity();
        double hoursElapsed = minutesElapsed / 60.0;
        double energyConsumed = powerOutput * hoursElapsed;
        
        // Calculate current cost
        double pricePerKwh = session.getChargingPoint().getPricePerKwh();
        double currentCost = energyConsumed * pricePerKwh;
        
        // Calculate time remaining (minutes)
        int timeRemaining = (int) Math.max(0, (100 - stateOfCharge) / socPerMinute);
        
        // Build update DTO
        return ChargingStatusUpdate.builder()
            .sessionId(session.getSessionId())
            .stateOfCharge(Math.round(stateOfCharge * 10.0) / 10.0) // Round to 1 decimal
            .energyConsumed(Math.round(energyConsumed * 100.0) / 100.0) // Round to 2 decimals
            .currentCost((double) Math.round(currentCost))
            .timeRemaining(timeRemaining)
            .status(session.getStatus().toString())
            .powerOutput(powerOutput)
            .startTime(session.getStartTime())
            .updateTime(now)
            .stationName(session.getChargingPoint().getChargingStation().getName())
            .chargerName(session.getChargingPoint().getPointName())
            .userEmail(session.getUser().getEmail())
            .alertMessage(stateOfCharge >= 90 ? "Sắp sạc đầy!" : null)
            .build();
    }
    
    /**
     * Handle manual status request from client
     * Client sends to: /app/charging/status/{sessionId}
     * Response sent to: /topic/charging/{sessionId}
     */
    @MessageMapping("/charging/status/{sessionId}")
    @SendTo("/topic/charging/{sessionId}")
    public ChargingStatusUpdate getChargingStatus(@DestinationVariable Long sessionId) {
        logger.info("Manual status request for session {}", sessionId);
        
        PhienSac session = phienSacRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return ChargingStatusUpdate.builder()
                .sessionId(sessionId)
                .status("NOT_FOUND")
                .alertMessage("Session not found")
                .build();
        }
        
        return buildStatusUpdate(session);
    }
}
