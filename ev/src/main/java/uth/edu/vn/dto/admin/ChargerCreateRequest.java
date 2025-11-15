package uth.edu.vn.dto.admin;

import uth.edu.vn.enums.ConnectorType;

public class ChargerCreateRequest {
    private String pointName;
    private String connectorType;
    private Double powerCapacity;
    private Double pricePerKwh;

    // Constructors
    public ChargerCreateRequest() {
    }

    public ChargerCreateRequest(String pointName, String connectorType, Double powerCapacity, Double pricePerKwh) {
        this.pointName = pointName;
        this.connectorType = connectorType;
        this.powerCapacity = powerCapacity;
        this.pricePerKwh = pricePerKwh;
    }

    // Getters and Setters
    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public Double getPowerCapacity() {
        return powerCapacity;
    }

    public void setPowerCapacity(Double powerCapacity) {
        this.powerCapacity = powerCapacity;
    }

    public Double getPricePerKwh() {
        return pricePerKwh;
    }

    public void setPricePerKwh(Double pricePerKwh) {
        this.pricePerKwh = pricePerKwh;
    }

    // Utility method
    public ConnectorType toConnectorType() throws IllegalArgumentException {
        return ConnectorType.valueOf(this.connectorType.toUpperCase());
    }
}