package com.example.fincredex.model.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO implements Serializable {

    private Long id;

    private BigDecimal loanAmount;

    private BigDecimal interestRate;

    private Integer termMonths;

    private LocalDateTime createdAt;

}
