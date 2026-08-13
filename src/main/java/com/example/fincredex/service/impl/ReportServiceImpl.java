package com.example.fincredex.service.impl;

import com.example.fincredex.exception.NotFoundException;
import com.example.fincredex.mapper.ReportMapper;
import com.example.fincredex.model.Constants.ApiErrorMessage;
import com.example.fincredex.model.dto.ReportDTO;
import com.example.fincredex.model.entities.Company;
import com.example.fincredex.model.entities.Report;
import com.example.fincredex.model.request.NewReportRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.repository.CompanyRepository;
import com.example.fincredex.repository.ReportRepository;
import com.example.fincredex.security.validation.AccessValidator;
import com.example.fincredex.service.ReportService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final CompanyRepository companyRepository;
    private final AccessValidator accessValidator;

    @Override
    public IamResponse<ReportDTO> createReport(NewReportRequest request, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + companyId));

        Report report = reportMapper.create(request);
        report.setCompany(company);
        report.setReportMonth(LocalDate.now());

        Report saved = reportRepository.save(report);
        ReportDTO reportDTO = reportMapper.toReportDto(saved);
        return IamResponse.success(reportDTO);
    }

    @Override
    public IamResponse<ReportDTO> updateReport(NewReportRequest request, Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + id));

        accessValidator.validateAdminOrOwnerAccess(report.getCompany().getOwner().getId());

        reportMapper.updateReport(request, id);
        Report saved = reportRepository.save(report);
        ReportDTO reportDTO = reportMapper.toReportDto(saved);
        return IamResponse.success(reportDTO);
    }

    public IamResponse<List<ReportDTO>> getReportsByCompanyId(Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new NotFoundException(
                                ApiErrorMessage.NOT_FOUND_BY_ID.getMessage(companyId)
                        )
                );

        accessValidator.validateAdminOrOwnerAccess(
                company.getOwner().getId()
        );

        List<ReportDTO> reports = reportRepository
                .findAllByCompany_Id(companyId)
                .stream()
                .map(reportMapper::toReportDto)
                .toList();

        return IamResponse.createSuccessful(reports);
    }
}
