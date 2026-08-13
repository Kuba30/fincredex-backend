package com.example.fincredex.repository;

import com.example.fincredex.model.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findAllByCompany_Owner_Id(Long ownerId);
}
