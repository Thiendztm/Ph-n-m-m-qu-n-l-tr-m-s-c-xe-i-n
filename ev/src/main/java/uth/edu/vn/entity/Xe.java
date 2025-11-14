package uth.edu.vn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "xe")
public class Xe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    private String make; // Hãng xe (Tesla, VinFast, etc.)
    
    private String model; // Model (Model 3, VF8, etc.)
    
    @Column(name = "plate_number", unique = true)
    private String plateNumber; // Biển số xe
    
    @Column(name = "plug_type")
    private String plugType; // Loại cổng sạc
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Xe() {}
    
    public Xe(Long userId, String make, String model, String plateNumber, String plugType) {
        this.userId = userId;
        this.make = make;
        this.model = model;
        this.plateNumber = plateNumber;
        this.plugType = plugType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business Methods from ClassDiagram
    public void addVehicle() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        System.out.println("Vehicle added: " + make + " " + model);
    }
    
    public void updateVehicle() {
        this.updatedAt = LocalDateTime.now();
        System.out.println("Vehicle updated: " + plateNumber);
    }
    
    public void removeVehicle() {
        System.out.println("Vehicle removed: " + plateNumber);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    
    public String getPlugType() { return plugType; }
    public void setPlugType(String plugType) { this.plugType = plugType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}