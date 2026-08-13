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
public class NewApplicationRequest implements Serializable {

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Loan amount must have at most 12 digits and 2 decimal places")
    private BigDecimal loanAmount;

    @NotNull(message = "Interest rate is required")
    @Positive(message = "Interest rate must be greater than 0")
    @DecimalMax(value = "100.00", message = "Interest rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Interest rate must have at most 3 digits and 2 decimal places")
    private BigDecimal interestRate;

    @NotNull(message = "Term months is required")
    @Min(value = 1, message = "Term must be at least 1 month")
    @Max(value = 360, message = "Term cannot exceed 360 months (30 years)")
    private Integer termMonths;
}
