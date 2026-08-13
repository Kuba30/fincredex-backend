package com.example.fincredex.repository;

import com.example.fincredex.model.entities.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {
    Optional<AiAnalysis> findByApplication_Id(Long applicationId);

    void deleteByApplication_Id(Long applicationId);
}
