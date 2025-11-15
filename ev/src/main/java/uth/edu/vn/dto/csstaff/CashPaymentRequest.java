// File: uth/edu/vn/dto/csstaff/CashPaymentRequest.java

package uth.edu.vn.dto.csstaff;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class CashPaymentRequest {

    @NotNull(message = "Session ID không được để trống")
    private Long sessionId;
}