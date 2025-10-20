package utd.edu.vn.model;

import java.math.BigDecimal;

public class Payment {
    private Long id;
    private Long sessionId;
    private BigDecimal amount;
    private String method;
    private String status;
    
    public Payment() {
    }
    
    public Payment(Long sessionId, BigDecimal amount, String method) {
        this.sessionId = sessionId;
        this.amount = amount;
        this.method = method;
        this.status = "PENDING";
    }
    
    // Methods from diagram
    public boolean processPayment() {
        System.out.println("Processing payment of " + amount + " via " + method);
        this.status = "COMPLETED";
        return true;
    }
    
    public boolean refundPayment() {
        return false;
    }
    
    public boolean validatePaymentMethod() {
        return false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}