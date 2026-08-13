package com.example.fincredex.model.dto;

import com.example.fincredex.model.entities.Application;
import com.example.fincredex.model.entities.Report;
import com.example.fincredex.model.entities.User;
import com.example.fincredex.model.enums.Industry;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO implements Serializable {
    private Long id;
    private String companyName;
    private String industry;
    private Integer companyAgeYears;
    private LocalDateTime createdAt;

}
