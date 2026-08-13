package com.example.fincredex.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailsDTO implements Serializable {

    private Long id;

    private CompanyDTO company;
    private ReportDTO report;
    private ApplicationDTO application;
}
