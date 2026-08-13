package com.example.fincredex.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "report_month", nullable = false)
    private LocalDate reportMonth;

    @Column(name = "monthly_revenue", nullable = false)
    private BigDecimal monthlyRevenue;

    @Column(name = "current_payments", nullable = false)
    private BigDecimal currentPayments;

    @Column(name = "late_payments_count", nullable = false)
    private Integer latePaymentsCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // N → 1 company
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // 1 → N applications
    @OneToMany(mappedBy = "report", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Application> applications = new ArrayList<>();

    // ---------------- Helpers ----------------

    public void addApplication(Application application) {
        applications.add(application);
        application.setReport(this);
    }

    public void removeApplication(Application application) {
        applications.remove(application);
        application.setReport(null);
    }

}