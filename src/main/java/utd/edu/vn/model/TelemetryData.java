package utd.edu.vn.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class TelemetryData {
    private Long id;
    private Long chargerId;
    private BigDecimal voltage;
    private BigDecimal current;
    private BigDecimal power;
    private BigDecimal temperature;
    private LocalDateTime timestamp;
    private Double soc; // State of Charge
    private Double currentPower;
    
    public TelemetryData() {
    }
    
    public TelemetryData(Long id, Long chargerId) {
        this.id = id;
        this.chargerId = chargerId;
    }
    
    // Methods from diagram
    public void recordData() {
    }
    
    public void analyzePattern() {
    }
    
    public void generateAlert() {
    }
    
    public void recordTelemetry() {
        System.out.println("Telemetry data recorded for charger " + chargerId);
    }
    
    public Double calculateEfficiency() {
        return 95.5; // Mock efficiency calculation
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getChargerId() {
        return chargerId;
    }
    
    public void setChargerId(Long chargerId) {
        this.chargerId = chargerId;
    }
    
    public BigDecimal getVoltage() {
        return voltage;
    }
    
    public void setVoltage(BigDecimal voltage) {
        this.voltage = voltage;
    }
    
    public BigDecimal getCurrent() {
        return current;
    }
    
    public void setCurrent(BigDecimal current) {
        this.current = current;
    }
    
    public BigDecimal getPower() {
        return power;
    }
    
    public void setPower(BigDecimal power) {
        this.power = power;
    }
    
    public BigDecimal getTemperature() {
        return temperature;
    }
    
    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Double getSoc() {
        return soc;
    }
    
    public void setSoc(Double soc) {
        this.soc = soc;
    }
    
    public Double getCurrentPower() {
        return currentPower;
    }
    
    public void setCurrentPower(Double currentPower) {
        this.currentPower = currentPower;
    }
    
    public void setVoltage(Double voltage) {
        this.voltage = BigDecimal.valueOf(voltage);
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = BigDecimal.valueOf(temperature);
    }
}