package utd.edu.vn.model;

public class Vehicle {
    private Long id;
    private Long userId;
    private String make;
    private String model;
    private String plateNumber;
    private String plugType;
    
    public Vehicle() {
    }
    
    public Vehicle(Long userId, String make, String model, String plateNumber, String plugType) {
        this.userId = userId;
        this.make = make;
        this.model = model;
        this.plateNumber = plateNumber;
        this.plugType = plugType;
    }
    
    // Methods from diagram
    public void addVehicle() {
        System.out.println("Vehicle added: " + make + " " + model);
    }
    
    public void updateVehicle() {
        System.out.println("Vehicle updated: " + make + " " + model);
    }
    
    public void removeVehicle() {
    }
    
    public String getVehicleInfo() {
        return make + " " + model + " (" + plateNumber + ") - " + plugType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getMake() {
        return make;
    }
    
    public void setMake(String make) {
        this.make = make;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getPlateNumber() {
        return plateNumber;
    }
    
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
    
    public String getPlugType() {
        return plugType;
    }
    
    public void setPlugType(String plugType) {
        this.plugType = plugType;
    }
}