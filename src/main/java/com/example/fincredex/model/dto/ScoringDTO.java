package com.example.fincredex.model.dto;



import com.example.fincredex.model.enums.Decision;
import com.example.fincredex.model.enums.Rating;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringDTO implements Serializable {

    private Long id;

    private BigDecimal newPayment;
    private BigDecimal debtLoad;
    private BigDecimal dscr;

    private Rating rating;
    private Decision decision;

    private LocalDateTime createdAt;
}
