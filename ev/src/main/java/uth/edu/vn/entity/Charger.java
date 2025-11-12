package uth.edu.vn.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import uth.edu.vn.enums.ConnectorType;
import uth.edu.vn.enums.PointStatus;

@Entity
@Table(name = "charger")
public class Charger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointId;
    
    @Column(nullable = true) // Tạm thời cho phép null để không bị lỗi migration
    private String pointName;
    
    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;
    
    @Column(nullable = true) // Tạm thời cho phép null để không bị lỗi migration
    private Double powerCapacity; // kW
    
    @Column(nullable = true) // Tạm thời cho phép null để không bị lỗi migration
    private Double pricePerKwh; // Price per kWh
    
    @Enumerated(EnumType.STRING)
    private PointStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private TramSac chargingStation;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "chargingPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatCho> bookings;
    
    @OneToMany(mappedBy = "chargingPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhienSac> chargingSessions;
    
    // Constructors
    public Charger() {}
    
    public Charger(String pointName, ConnectorType connectorType, Double powerCapacity, Double pricePerKwh, TramSac chargingStation) {
        this.pointName = pointName;
        this.connectorType = connectorType;
        this.powerCapacity = powerCapacity;
        this.pricePerKwh = pricePerKwh;
        this.chargingStation = chargingStation;
        this.status = PointStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getPointId() { return pointId; }
    public void setPointId(Long pointId) { this.pointId = pointId; }
    
    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }
    
    public ConnectorType getConnectorType() { return connectorType; }
    public void setConnectorType(ConnectorType connectorType) { this.connectorType = connectorType; }
    
    public Double getPowerCapacity() { return powerCapacity; }
    public void setPowerCapacity(Double powerCapacity) { this.powerCapacity = powerCapacity; }
    
    public Double getPricePerKwh() { return pricePerKwh; }
    public void setPricePerKwh(Double pricePerKwh) { this.pricePerKwh = pricePerKwh; }
    
    public PointStatus getStatus() { return status; }
    public void setStatus(PointStatus status) { this.status = status; }
    
    public TramSac getChargingStation() { return chargingStation; }
    public void setChargingStation(TramSac chargingStation) { this.chargingStation = chargingStation; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<DatCho> getBookings() { return bookings; }
    public void setBookings(List<DatCho> bookings) { this.bookings = bookings; }
    
    public List<PhienSac> getChargingSessions() { return chargingSessions; }
    public void setChargingSessions(List<PhienSac> chargingSessions) { this.chargingSessions = chargingSessions; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}