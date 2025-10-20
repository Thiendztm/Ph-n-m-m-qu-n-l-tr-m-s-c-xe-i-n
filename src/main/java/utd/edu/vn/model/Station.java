package utd.edu.vn.model;

import java.util.List;

public class Station {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String status;
    
    public Station() {
    }
    
    public Station(String name, String address, Double latitude, Double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = "ACTIVE";
    }
    
    // Methods from diagram
    public void addStation() {
        System.out.println("Station added: " + name);
    }
    
    public void updateStation() {
    }
    
    public List<Charger> getAvailableChargers() {
        return null;
    }
    
    public void addCharger(Charger charger) {
        System.out.println("Charger added to station: " + charger.getConnectorType());
    }
    
    public int getAvailableChargersCount() {
        return 2; // Mock response for testing
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Station{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}