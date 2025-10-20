package utd.edu.vn.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChargingSession {
    private Long id;
    private Long userId;
    private Long chargerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double energyDelivered;
    private BigDecimal cost;
    
    public ChargingSession() {
    }
    
    public ChargingSession(Long userId, Long chargerId) {
        this.userId = userId;
        this.chargerId = chargerId;
    }
    
    // Methods from diagram
    public void startSession() {
        System.out.println("Charging session started");
    }
    
    public void stopSession() {
        System.out.println("Charging session stopped");
    }
    
    public void pauseSession() {
    }
    
    public void updateEnergyDelivered(Double energy) {
        this.energyDelivered = energy;
        System.out.println("Energy delivered updated: " + energy + " kWh");
    }
    
    public BigDecimal calculateCost() {
        return null;
    }
    
    public Long getDuration() {
        return null;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getChargerId() {
        return chargerId;
    }
    
    public void setChargerId(Long chargerId) {
        this.chargerId = chargerId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Double getEnergyDelivered() {
        return energyDelivered;
    }
    
    public void setEnergyDelivered(Double energyDelivered) {
        this.energyDelivered = energyDelivered;
    }
    
    public BigDecimal getCost() {
        return cost;
    }
    
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
    
    public String getStatus() {
        return null;
    }
}