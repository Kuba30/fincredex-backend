package com.example.fincredex.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // N → 1 company
    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // N → 1 financial snapshot
    @ManyToOne(fetch = FetchType.LAZY,   optional = false)
    @JoinColumn(name = "financial_id", nullable = false)
    private Report report;

    // 1 → 1 scoring
    @OneToOne(mappedBy = "application", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Scoring scoring;

    // 1 → N AI analysis
    @OneToMany(mappedBy = "application", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<AiAnalysis> aiAnalyses = new ArrayList<>();

    public void addAiAnalysis(AiAnalysis analysis) {
        aiAnalyses.add(analysis);
        analysis.setApplication(this);
    }

    public void removeAiAnalysis(AiAnalysis analysis) {
        aiAnalyses.remove(analysis);
        analysis.setApplication(null);
    }

    public void setScoringResult(Scoring result) {
        this.scoring = result;
        if (result != null) {
            result.setApplication(this);
        }
    }
}