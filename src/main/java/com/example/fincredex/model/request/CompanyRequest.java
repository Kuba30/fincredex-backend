package com.example.fincredex.model.request;

import com.example.fincredex.model.enums.Industry;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CompanyRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Industry is required")
    private Industry industry;

    @NotNull(message = "Company age is required")
    @Min(value = 0, message = "Company age cannot be negative")
    private Integer companyAgeYears;
}
