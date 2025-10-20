package utd.edu.vn.model;

public class Charger {
    private Long id;
    private Long stationId;
    private String connectorType;
    private Double maxPower;
    private String status;
    
    public Charger() {
    }
    
    public Charger(Long stationId, String connectorType, Double maxPower) {
        this.stationId = stationId;
        this.connectorType = connectorType;
        this.maxPower = maxPower;
        this.status = "AVAILABLE";
    }
    
    // Methods from diagram
    public boolean startCharging() {
        System.out.println("Charging started on " + connectorType + " charger");
        return true;
    }
    
    public boolean stopCharging() {
        System.out.println("Charging stopped on " + connectorType + " charger");
        return true;
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
    
    public String getConnectorType() {
        return connectorType;
    }
    
    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }
    
    public Double getMaxPower() {
        return maxPower;
    }
    
    public void setMaxPower(Double maxPower) {
        this.maxPower = maxPower;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}