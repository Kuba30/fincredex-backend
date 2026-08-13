package com.example.fincredex.repository;

import com.example.fincredex.model.dto.ReportDTO;
import com.example.fincredex.model.entities.Report;
import com.example.fincredex.model.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    Optional<Report> findById(Long companyId);

    List<Report> findAllByCompany_Id(Long companyId);
}
