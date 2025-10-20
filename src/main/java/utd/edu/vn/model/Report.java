package utd.edu.vn.model;

import java.time.LocalDateTime;

public class Report {
    private Long id;
    private String title;
    private String type;
    private Long generatedBy;
    private String data;
    private LocalDateTime generatedAt;
    
    public Report() {
    }
    
    public Report(String title, String type, Long generatedBy) {
        this.title = title;
        this.type = type;
        this.generatedBy = generatedBy;
    }
    
    // Methods from diagram
    public void generateReport() {
        System.out.println("Report generated: " + title);
    }
    
    public byte[] exportToPdf() {
        System.out.println("Report exported to PDF: " + title);
        return null;
    }
    
    public byte[] exportToExcel() {
        return null;
    }
    
    public void scheduleReport() {
        System.out.println("Report scheduled: " + title);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getGeneratedBy() {
        return generatedBy;
    }
    
    public void setGeneratedBy(Long generatedBy) {
        this.generatedBy = generatedBy;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}