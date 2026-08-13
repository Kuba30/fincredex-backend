package com.example.fincredex.repository;

import com.example.fincredex.model.entities.Company;
import com.example.fincredex.model.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findById(Long companyId );

    List<Company> findAllByOwner_Id(Long ownerId);
}
