package utd.edu.vn.model;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private Long userId;
    private Long chargerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    
    public Reservation() {
    }
    
    public Reservation(Long userId, Long chargerId, LocalDateTime startTime, LocalDateTime endTime) {
        this.userId = userId;
        this.chargerId = chargerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "PENDING";
    }
    
    // Methods from diagram
    public void createReservation() {
        System.out.println("Reservation created for charger " + chargerId);
    }
    
    public void cancelReservation() {
    }
    
    public void confirmArrival() {
        System.out.println("Arrival confirmed for reservation " + id);
    }
    
    public boolean isExpired() {
        return false;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}