package utd.edu.vn.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    private Long id;
    private Long paymentId;
    private String invoiceNumber;
    private BigDecimal totalAmount;
    private LocalDateTime issueDate;
    
    public Invoice() {
    }
    
    public Invoice(Long paymentId, BigDecimal totalAmount) {
        this.paymentId = paymentId;
        this.totalAmount = totalAmount;
        this.invoiceNumber = "INV-" + System.currentTimeMillis();
    }
    
    // Methods from diagram
    public void generateInvoice() {
        System.out.println("Invoice generated: " + invoiceNumber);
    }
    
    public void sendInvoiceEmail() {
        System.out.println("Invoice email sent for " + invoiceNumber);
    }
    
    public String getInvoiceDetails() {
        return "Invoice: " + invoiceNumber + "\nAmount: " + totalAmount + "\nPayment ID: " + paymentId;
    }
    
    public byte[] downloadPdf() {
        return null;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }
}