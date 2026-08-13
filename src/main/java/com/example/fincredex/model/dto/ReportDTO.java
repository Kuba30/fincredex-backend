package com.example.fincredex.model.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO implements Serializable {
    private Long id;

    private LocalDate reportMonth;

    private BigDecimal monthlyRevenue;

    private BigDecimal currentPayments;

    private Integer latePaymentsCount;

    private LocalDateTime createdAt;

}
