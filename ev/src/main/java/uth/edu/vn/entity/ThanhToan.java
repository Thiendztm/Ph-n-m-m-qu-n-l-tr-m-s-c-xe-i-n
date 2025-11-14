package uth.edu.vn.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan")
public class ThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_method")
    private String method;
    
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public ThanhToan() {}
    
    public ThanhToan(Long sessionId, BigDecimal amount, String method) {
        this.sessionId = sessionId;
        this.amount = amount;
        this.method = method;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business Methods from ClassDiagram
    public boolean processPayment() {
        if (validatePaymentMethod()) {
            boolean success = chargeCard();
            if (success) {
                this.status = "COMPLETED";
                updateWallet();
                this.updatedAt = LocalDateTime.now();
                System.out.println("Payment processed successfully: " + amount);
                return true;
            }
        }
        this.status = "FAILED";
        this.updatedAt = LocalDateTime.now();
        return false;
    }
    
    public boolean refundPayment() {
        if ("COMPLETED".equals(this.status)) {
            this.status = "REFUNDED";
            this.updatedAt = LocalDateTime.now();
            System.out.println("Payment refunded: " + amount);
            return true;
        }
        return false;
    }
    
    public boolean validatePaymentMethod() {
        return method != null && !method.trim().isEmpty();
    }
    
    private boolean chargeCard() {
        // Simulate card charging logic
        System.out.println("Charging card for amount: " + amount);
        return true; // Assume success for demo
    }
    
    private void updateWallet() {
        // Update wallet logic
        System.out.println("Wallet updated after payment");
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}