package uth.edu.vn.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dieu_khien_tu_xa")
public class DieuKhienTuXa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long telemetryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charging_point_id", nullable = false)
    private Charger chargingPoint;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Xe vehicle;
    
    @Column(name = "soc_percentage") // State of Charge
    private Double socPercentage;
    
    @Column(name = "current_power_kw")
    private Double currentPowerKw;
    
    @Column(name = "energy_delivered_kwh")
    private Double energyDeliveredKwh;
    
    @Column(name = "voltage_v")
    private Double voltageV;
    
    @Column(name = "current_a")
    private Double currentA;
    
    @Column(name = "temperature_c")
    private Double temperatureC;
    
    @Column(name = "charging_duration_minutes")
    private Integer chargingDurationMinutes;
    
    @Enumerated(EnumType.STRING)
    private TelemetryStatus status;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
    
    @Column(name = "error_code")
    private String errorCode;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    // Constructors
    public DieuKhienTuXa() {}
    
    public DieuKhienTuXa(Charger chargingPoint) {
        this.chargingPoint = chargingPoint;
        this.recordedAt = LocalDateTime.now();
        this.status = TelemetryStatus.NORMAL;
    }
    
    // Getters and Setters
    public Long getTelemetryId() { return telemetryId; }
    public void setTelemetryId(Long telemetryId) { this.telemetryId = telemetryId; }
    
    public Charger getChargingPoint() { return chargingPoint; }
    public void setChargingPoint(Charger chargingPoint) { this.chargingPoint = chargingPoint; }
    
    public Xe getVehicle() { return vehicle; }
    public void setVehicle(Xe vehicle) { this.vehicle = vehicle; }
    
    public Double getSocPercentage() { return socPercentage; }
    public void setSocPercentage(Double socPercentage) { this.socPercentage = socPercentage; }
    
    public Double getCurrentPowerKw() { return currentPowerKw; }
    public void setCurrentPowerKw(Double currentPowerKw) { this.currentPowerKw = currentPowerKw; }
    
    public Double getEnergyDeliveredKwh() { return energyDeliveredKwh; }
    public void setEnergyDeliveredKwh(Double energyDeliveredKwh) { this.energyDeliveredKwh = energyDeliveredKwh; }
    
    public Double getVoltageV() { return voltageV; }
    public void setVoltageV(Double voltageV) { this.voltageV = voltageV; }
    
    public Double getCurrentA() { return currentA; }
    public void setCurrentA(Double currentA) { this.currentA = currentA; }
    
    public Double getTemperatureC() { return temperatureC; }
    public void setTemperatureC(Double temperatureC) { this.temperatureC = temperatureC; }
    
    public Integer getChargingDurationMinutes() { return chargingDurationMinutes; }
    public void setChargingDurationMinutes(Integer chargingDurationMinutes) { this.chargingDurationMinutes = chargingDurationMinutes; }
    
    public TelemetryStatus getStatus() { return status; }
    public void setStatus(TelemetryStatus status) { this.status = status; }
    
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

enum TelemetryStatus {
    NORMAL,
    WARNING,
    ERROR,
    OFFLINE
}