package uth.edu.vn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tram_sac")
public class TramSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String address;
    
    @Column(nullable = false)
    private Double latitude;
    
    @Column(nullable = false)
    private Double longitude;
    
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "chargingStation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Charger> chargingPoints;
    
    // Constructors
    public TramSac() {}
    
    public TramSac(String name, String address, Double latitude, Double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = "ONLINE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business Methods from ClassDiagram
    public void addStation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        System.out.println("Station added: " + name);
    }
    
    public void updateStation() {
        this.updatedAt = LocalDateTime.now();
        System.out.println("Station updated: " + name);
    }
    
    public List<Charger> getAvailableChargers() {
        // Logic to filter available chargers
        return chargingPoints.stream()
                .filter(charger -> uth.edu.vn.enums.PointStatus.AVAILABLE.equals(charger.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Charger> getChargingPoints() { return chargingPoints; }
    public void setChargingPoints(List<Charger> chargingPoints) { this.chargingPoints = chargingPoints; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}