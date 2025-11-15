package uth.edu.vn.dto.admin;

import uth.edu.vn.enums.StationStatus;

public class StationResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private StationStatus status;
    private int totalChargers;
    private long availableChargers;

    // Constructors
    public StationResponse() {
    }

    public StationResponse(Long id, String name, String address, Double latitude, Double longitude,
            StationStatus status, int totalChargers, long availableChargers) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.totalChargers = totalChargers;
        this.availableChargers = availableChargers;
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

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }

    public int getTotalChargers() {
        return totalChargers;
    }

    public void setTotalChargers(int totalChargers) {
        this.totalChargers = totalChargers;
    }

    public long getAvailableChargers() {
        return availableChargers;
    }

    public void setAvailableChargers(long availableChargers) {
        this.availableChargers = availableChargers;
    }
}