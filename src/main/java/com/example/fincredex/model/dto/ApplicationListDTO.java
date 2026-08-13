package com.example.fincredex.model.dto;


import com.example.fincredex.model.enums.Decision;
import com.example.fincredex.model.enums.Rating;
import com.example.fincredex.model.enums.RiskLevel;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationListDTO implements Serializable {

    private Long id;

    private Long companyId;
    private String companyName;

    private BigDecimal loanAmount;

    private Rating rating;
    private Decision decision;

    private RiskLevel riskLevel;
    private Integer confidenceScore;

    private LocalDateTime createdAt;
}
