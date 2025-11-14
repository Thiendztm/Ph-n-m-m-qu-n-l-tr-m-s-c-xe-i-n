package uth.edu.vn.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import uth.edu.vn.enums.SubscriptionStatus;

@Entity
@Table(name = "goi_dich_vu")
public class GoiDichVu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Column(name = "monthly_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFee;
    
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public GoiDichVu() {}
    
    public GoiDichVu(User user, String planName, BigDecimal monthlyFee) {
        this.user = user;
        this.planName = planName;
        this.monthlyFee = monthlyFee;
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business Methods
    public void subscribe() {
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancelSubscription() {
        this.status = SubscriptionStatus.CANCELLED;
        this.endDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void renewSubscription() {
        this.status = SubscriptionStatus.ACTIVE;
        this.endDate = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && 
               (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }
    
    public BigDecimal calculateDiscount() {
        // Basic discount logic - can be enhanced based on business rules
        if (monthlyFee.compareTo(new BigDecimal("200000")) >= 0) {
            return monthlyFee.multiply(new BigDecimal("0.1")); // 10% discount for premium plans
        }
        return BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    
    public BigDecimal getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(BigDecimal monthlyFee) { this.monthlyFee = monthlyFee; }
    
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}