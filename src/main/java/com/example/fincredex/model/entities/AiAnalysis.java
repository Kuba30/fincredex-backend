package com.example.fincredex.model.entities;

import com.example.fincredex.model.enums.RiskLevel;
import com.example.fincredex.model.enums.enum_converter.RiskLevelConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_analysis")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Convert(converter = RiskLevelConverter.class)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "strengths")
    private String strengths;

    @Column(name = "weaknesses")
    private String weaknesses;

    @Column(name = "recommendation")
    private String recommendation;

    @Column(name = "raw_response")
    private String rawResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // N → 1 application
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private String metricAnalysis;

    @Column(nullable = false)
    private String stopFactors;

    @Column(nullable = false)
    private String reasoning;

    @Column(nullable = false)
    private Integer confidenceScore;
}