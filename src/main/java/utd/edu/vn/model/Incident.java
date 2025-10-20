package utd.edu.vn.model;

import java.time.LocalDateTime;

public class Incident {
    private Long id;
    private Long stationId;
    private Long chargerId;
    private Long reportedBy;
    private Long assignedTo;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime reportedAt;
    
    public Incident() {
    }
    
    public Incident(Long reportedBy, Long stationId, Long chargerId, String description) {
        this.reportedBy = reportedBy;
        this.stationId = stationId;
        this.chargerId = chargerId;
        this.description = description;
        this.status = "REPORTED";
    }
    
    // Methods from diagram
    public void reportIncident() {
        System.out.println("Incident reported: " + description);
    }
    
    public void assignToTechnician(Long techId) {
        this.assignedTo = techId;
        System.out.println("Incident assigned to technician: " + techId);
    }
    
    public void updateStatus(String newStatus) {
        this.status = newStatus;
        System.out.println("Incident status updated to: " + newStatus);
    }
    
    public void resolveIncident() {
        this.status = "RESOLVED";
        System.out.println("Incident resolved: " + description);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getStationId() {
        return stationId;
    }
    
    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }
    
    public Long getChargerId() {
        return chargerId;
    }
    
    public void setChargerId(Long chargerId) {
        this.chargerId = chargerId;
    }
    
    public Long getReportedBy() {
        return reportedBy;
    }
    
    public void setReportedBy(Long reportedBy) {
        this.reportedBy = reportedBy;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getReportedAt() {
        return reportedAt;
    }
    
    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }
    
    public Long getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }
}