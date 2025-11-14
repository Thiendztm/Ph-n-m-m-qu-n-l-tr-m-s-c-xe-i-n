package uth.edu.vn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import uth.edu.vn.enums.SessionStatus;

@Entity
@Table(name = "phien_sac")
public class PhienSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    private Charger chargingPoint;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "energy_consumed")
    private Double energyConsumed; // kWh
    
    @Column(name = "start_soc")
    private Integer startSoc; // State of Charge %
    
    @Column(name = "end_soc")
    private Integer endSoc; // State of Charge %
    
    @Column(name = "total_cost")
    private Double totalCost;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    private String qrCode; // QR Code for session identification
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PhienSac() {}
    
    public PhienSac(User user, Charger chargingPoint, String qrCode) {
        this.user = user;
        this.chargingPoint = chargingPoint;
        this.qrCode = qrCode;
        this.startTime = LocalDateTime.now();
        this.status = SessionStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Charger getChargingPoint() { return chargingPoint; }
    public void setChargingPoint(Charger chargingPoint) { this.chargingPoint = chargingPoint; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Double getEnergyConsumed() { return energyConsumed; }
    public void setEnergyConsumed(Double energyConsumed) { this.energyConsumed = energyConsumed; }
    
    public Integer getStartSoc() { return startSoc; }
    public void setStartSoc(Integer startSoc) { this.startSoc = startSoc; }
    
    public Integer getEndSoc() { return endSoc; }
    public void setEndSoc(Integer endSoc) { this.endSoc = endSoc; }
    
    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
    
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}