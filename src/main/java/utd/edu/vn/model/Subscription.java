package utd.edu.vn.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class Subscription {
    private Long id;
    private Long userId;
    private String planName;
    private BigDecimal monthlyFee;
    private String benefits;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    
    public Subscription() {
    }
    
    public Subscription(Long userId, String planName, BigDecimal monthlyFee) {
        this.userId = userId;
        this.planName = planName;
        this.monthlyFee = monthlyFee;
        this.status = "ACTIVE";
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusMonths(1);
    }
    
    // Methods from diagram
    public void activateSubscription() {
    }
    
    public void pauseSubscription() {
    }
    
    public void cancelSubscription() {
    }
    
    public void renewSubscription() {
    }
    
    public void upgradeSubscription() {
    }
    
    public void subscribe() {
        System.out.println("Subscribed to plan: " + planName);
    }
    
    public long getDaysRemaining() {
        return 25; // Mock value for testing
    }
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
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
    
    public String getPlanName() {
        return planName;
    }
    
    public void setPlanName(String planName) {
        this.planName = planName;
    }
    
    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }
    
    public void setMonthlyFee(BigDecimal monthlyFee) {
        this.monthlyFee = monthlyFee;
    }
    
    public String getBenefits() {
        return benefits;
    }
    
    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}