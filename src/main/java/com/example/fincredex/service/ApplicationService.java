package com.example.fincredex.service;

import com.example.fincredex.model.dto.ApplicationDTO;
import com.example.fincredex.model.dto.ApplicationDetailsDTO;
import com.example.fincredex.model.dto.ApplicationListDTO;
import com.example.fincredex.model.dto.ReportDTO;
import com.example.fincredex.model.entities.Application;
import com.example.fincredex.model.request.NewApplicationRequest;
import com.example.fincredex.model.request.NewReportRequest;
import com.example.fincredex.model.response.IamResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;


public interface ApplicationService {

    IamResponse<ApplicationDTO> createApplication(@NotNull @Valid NewApplicationRequest request, Long reportId);

    IamResponse<ApplicationDTO> updateApplication(@NotNull @Valid NewApplicationRequest request, Long id);

    IamResponse<ApplicationDTO> deleteApplication(Long AppId);

    IamResponse<ApplicationDTO> getApplicationById(Long id);

    IamResponse<List<ApplicationListDTO>> getAllApplications();

    IamResponse<ApplicationDetailsDTO> getApplicationDetails(Long id);
}
