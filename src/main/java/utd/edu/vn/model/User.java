package utd.edu.vn.model;

import utd.edu.vn.enums.UserRole;
import java.math.BigDecimal;

public class User {
    private Long id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private UserRole role;
    private BigDecimal walletBalance;
    
    public User() {
    }
    
    public User(String email, String phoneNumber, String firstName, String lastName, UserRole role) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.walletBalance = BigDecimal.ZERO;
    }
    
    // Methods from diagram
    public void login() {
        System.out.println("User " + firstName + " " + lastName + " logged in successfully");
    }
    
    public void logout() {
    }
    
    public void updateProfile() {
        System.out.println("Profile updated for " + firstName + " " + lastName);
    }
    
    public void addFunds(BigDecimal amount) {
        if (this.walletBalance == null) {
            this.walletBalance = BigDecimal.ZERO;
        }
        this.walletBalance = this.walletBalance.add(amount);
        System.out.println("Added " + amount + " to wallet. New balance: " + this.walletBalance);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public BigDecimal getWalletBalance() {
        return walletBalance;
    }
    
    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", walletBalance=" + walletBalance +
                '}';
    }
}