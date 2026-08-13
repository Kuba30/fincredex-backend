package com.example.fincredex.model.entities;

import com.example.fincredex.model.enums.Decision;
import com.example.fincredex.model.enums.Rating;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "scoring")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Scoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "new_payment", nullable = false)
    private BigDecimal newPayment;

    @Column(name = "debt_load", nullable = false)
    private BigDecimal debtLoad;

    @Column(name = "dscr", nullable = false)
    private BigDecimal dscr;

    @Column(name = "rating", nullable = false)
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @Column(name = "decision", nullable = false)
    @Enumerated(EnumType.STRING)
    private Decision decision;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 1 → 1 application
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;


}