package com.example.fincredex.repository;

import com.example.fincredex.model.entities.Scoring;
import com.example.fincredex.model.response.IamResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoringRepository extends JpaRepository<Scoring, Integer> {

    Optional<Scoring> findByApplication_Id(Long applicationId);

    void deleteByApplication_Id(Long applicationId);


}

