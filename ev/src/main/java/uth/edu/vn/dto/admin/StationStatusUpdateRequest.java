package uth.edu.vn.dto.admin;

import uth.edu.vn.enums.StationStatus;

public class StationStatusUpdateRequest {
    private String status;

    // Constructors
    public StationStatusUpdateRequest() {
    }

    public StationStatusUpdateRequest(String status) {
        this.status = status;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Utility method
    public StationStatus toStationStatus() throws IllegalArgumentException {
        return StationStatus.valueOf(this.status.toUpperCase());
    }
}