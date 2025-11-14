package uth.edu.vn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import uth.edu.vn.enums.IncidentSeverity;
import uth.edu.vn.enums.IncidentStatus;

@Entity
@Table(name = "su_co")
public class SuCo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private TramSac chargingStation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private Charger chargingPoint;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy; // Người báo cáo (có thể là staff hoặc driver)
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000, nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    
    @Enumerated(EnumType.STRING)
    private IncidentStatus status;
    
    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo; // Nhân viên được giao xử lý
    
    @Column(length = 2000)
    private String resolution; // Mô tả cách giải quyết
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public SuCo() {}
    
    public SuCo(String title, String description, IncidentSeverity severity, User reportedBy) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.reportedBy = reportedBy;
        this.status = IncidentStatus.REPORTED;
        this.reportedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }
    
    public TramSac getChargingStation() { return chargingStation; }
    public void setChargingStation(TramSac chargingStation) { this.chargingStation = chargingStation; }
    
    public Charger getChargingPoint() { return chargingPoint; }
    public void setChargingPoint(Charger chargingPoint) { this.chargingPoint = chargingPoint; }
    
    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }
    
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}