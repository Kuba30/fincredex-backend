package com.example.fincredex.service;

import com.example.fincredex.model.dto.CompanyDTO;
import com.example.fincredex.model.dto.ReportDTO;
import com.example.fincredex.model.request.CompanyRequest;
import com.example.fincredex.model.request.NewReportRequest;
import com.example.fincredex.model.response.IamResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface CompanyService {

    IamResponse<CompanyDTO> createCompany(@NotNull @Valid CompanyRequest request, String email);

    IamResponse<List<CompanyDTO>> getAllCompanies();

    IamResponse<CompanyDTO> getCompanyById(Long id);
}
