// File: uth/edu/vn/dto/csstaff/IncidentReportRequest.java

package uth.edu.vn.dto.csstaff;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class IncidentReportRequest {

    @NotNull(message = "Station ID không được để trống")
    private Long stationId;

    @NotNull(message = "Point ID không được để trống")
    private Long pointId;

    @NotNull(message = "Mô tả sự cố không được để trống")
    @Size(min = 10, message = "Mô tả sự cố phải có ít nhất 10 ký tự")
    private String description;
}