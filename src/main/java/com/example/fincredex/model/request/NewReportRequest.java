package com.example.fincredex.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewReportRequest implements Serializable {

//    private String companyName;

    @NotNull(message = "Monthly revenue is required")
    @Positive(message = "Monthly revenue must be greater than 0")
    private BigDecimal monthlyRevenue;

    @NotNull(message = "Current payments is required")
    @PositiveOrZero(message = "Current payments cannot be negative")
    private BigDecimal currentPayments;

    @NotNull(message = "Late payments count is required")
    @Min(value = 0, message = "Late payments count cannot be negative")
    private Integer latePaymentsCount;
}
