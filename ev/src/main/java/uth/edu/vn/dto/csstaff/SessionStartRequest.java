// File: uth/edu/vn/dto/csstaff/SessionStartRequest.java

package uth.edu.vn.dto.csstaff;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SessionStartRequest {

    @NotNull(message = "Point ID không được để trống")
    private Long pointId;

    private String vehiclePlate;
}