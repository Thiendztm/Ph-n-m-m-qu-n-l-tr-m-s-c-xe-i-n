package uth.edu.vn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for real-time charging status updates
 * Sent via WebSocket to subscribed clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargingStatusUpdate {
    
    private Long sessionId;
    
    // Battery State of Charge (0-100%)
    private Double stateOfCharge;
    
    // Energy consumed so far (kWh)
    private Double energyConsumed;
    
    // Current cost (VND)
    private Double currentCost;
    
    // Time remaining (minutes)
    private Integer timeRemaining;
    
    // Session status
    private String status; // CHARGING, COMPLETED, STOPPED, ERROR
    
    // Power output (kW)
    private Double powerOutput;
    
    // Start time
    private LocalDateTime startTime;
    
    // Update timestamp
    private LocalDateTime updateTime;
    
    // Station and charger info
    private String stationName;
    private String chargerName;
    
    // User info
    private String userEmail;
    
    // Alert message (if any)
    private String alertMessage;
}
