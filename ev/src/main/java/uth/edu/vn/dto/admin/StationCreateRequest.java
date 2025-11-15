package uth.edu.vn.dto.admin;

public class StationCreateRequest {
    // @NotBlank
    private String name;
    // @NotBlank
    private String address;
    // @NotNull
    private Double latitude;
    // @NotNull
    private Double longitude;
    private String operatingHours = "24/7";

    // Constructors
    public StationCreateRequest() {
    }

    public StationCreateRequest(String name, String address, Double latitude, Double longitude, String operatingHours) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.operatingHours = operatingHours;
    }

    // Getters and Setters
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

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }
}