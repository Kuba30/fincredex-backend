package com.example.fincredex.service;

import com.example.fincredex.model.dto.ReportDTO;
import com.example.fincredex.model.request.NewReportRequest;
import com.example.fincredex.model.response.IamResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface ReportService {

    IamResponse<ReportDTO> createReport(@NotNull @Valid NewReportRequest request, Long companyId);

    IamResponse<ReportDTO> updateReport(@NotNull @Valid NewReportRequest request, Long id);

    IamResponse<List<ReportDTO>> getReportsByCompanyId(Long companyId);
}
