// File: uth/edu/vn/dto/csstaff/SessionStopRequest.java

package uth.edu.vn.dto.csstaff;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class SessionStopRequest {

    @NotNull(message = "Năng lượng tiêu thụ không được để trống")
    @Min(value = 0, message = "Năng lượng tiêu thụ phải lớn hơn hoặc bằng 0")
    private Double energyConsumed;

    @NotNull(message = "SOC cuối cùng không được để trống")
    @Min(value = 0, message = "SOC phải lớn hơn hoặc bằng 0")
    private Integer endSoc;
}