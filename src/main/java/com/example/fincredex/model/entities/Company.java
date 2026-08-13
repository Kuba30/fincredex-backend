package com.example.fincredex.model.entities;

import com.example.fincredex.model.enums.Industry;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Size(max = 100)
    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "industry", nullable = false, length = 50)
    private Industry industry;

    @Column(name = "company_age_years", nullable = false)
    private Integer companyAgeYears;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 1 → N monthly_financials
    @OneToMany(mappedBy = "company", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    // 1 → N applications
    @OneToMany(mappedBy = "company", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Application> applications = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    // ---------------- Helpers ----------------

    public void addFinancial(Report report) {
        reports.add(report);
        report.setCompany(this);
    }

    public void removeFinancial(Report report) {
        reports.remove(report);
        report.setCompany(null);
    }

    public void addApplication(Application application) {
        applications.add(application);
        application.setCompany(this);
    }

    public void removeApplication(Application application) {
        applications.remove(application);
        application.setCompany(null);
    }

}