package com.example.fincredex.service.impl;

import com.example.fincredex.exception.NotFoundException;
import com.example.fincredex.mapper.ApplicationMapper;
import com.example.fincredex.mapper.CompanyMapper;
import com.example.fincredex.mapper.ReportMapper;
import com.example.fincredex.model.Constants.ApiErrorMessage;
import com.example.fincredex.model.dto.ApplicationDTO;
import com.example.fincredex.model.dto.ApplicationDetailsDTO;
import com.example.fincredex.model.dto.ApplicationListDTO;
import com.example.fincredex.model.entities.*;
import com.example.fincredex.model.request.NewApplicationRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.repository.*;
import com.example.fincredex.security.validation.AccessValidator;
import com.example.fincredex.service.ApplicationService;
import com.example.fincredex.service.ScoringService;
import com.example.fincredex.utils.ApiUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private final ReportRepository reportRepository;
    private final ApplicationMapper applicationMapper;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final ScoringCalculation scoringCalculation;
    private final AiAnalysisService aiAnalysisService;
    private final AccessValidator accessValidator;
    private final ApiUtils apiUtils;
    private final CompanyMapper companyMapper;
    private final ReportMapper reportMapper;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final ScoringRepository scoringRepository;;



    @Transactional
    @Override
    public IamResponse<ApplicationDTO> createApplication(NewApplicationRequest request, Long reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));

        Company company = companyRepository.findById(report.getCompany().getId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + report.getCompany().getId()));

        Application application = applicationMapper.create(request);
        application.setCompany(company);
        application.setReport(report);
        application.setCreatedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        ApplicationDTO applicationDTO = applicationMapper.toApplicationDto(saved);

        scoringCalculation.calculateAndSave(saved);

        return IamResponse.success(applicationDTO);
    }

    @Transactional
    public IamResponse<ApplicationDTO> updateApplication(
            NewApplicationRequest request,
            Long applicationId
    ) {
        Application application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Application not found"
                                )
                        );

        application.setLoanAmount(request.getLoanAmount());
        application.setInterestRate(request.getInterestRate());
        application.setTermMonths(request.getTermMonths());

        applicationRepository.save(application);


        aiAnalysisRepository
                .findByApplication_Id(applicationId)
                .ifPresent(aiAnalysisRepository::delete);


        scoringRepository
                .findByApplication_Id(applicationId)
                .ifPresent(scoringRepository::delete);


        scoringRepository.flush();


        scoringCalculation.calculateAndSave(application);

        return new IamResponse<>(
                "",
                applicationMapper.toDto(application),
                true
        );
    }

    @Transactional
    @Override
    public IamResponse<ApplicationDTO> deleteApplication(Long Id) {
        Application application = applicationRepository.findById(Id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.NOT_FOUND_BY_ID.getMessage(Id)));

        accessValidator.validateAdminOrOwnerAccess(application.getCompany().getOwner().getId());


        aiAnalysisService.deleteAnalyze(application.getId());
        applicationRepository.delete(application);
        return IamResponse.success(applicationMapper.toDto(application));
    }

    public IamResponse<ApplicationDTO> getApplicationById(Long id) {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                ApiErrorMessage.NOT_FOUND_BY_ID.getMessage(id)
                        )
                );

        accessValidator.validateAdminOrOwnerAccess(
                application.getCompany().getOwner().getId()
        );

        ApplicationDTO applicationDTO =
                applicationMapper.toDto(application);

        return IamResponse.createSuccessful(applicationDTO);
    }

//    public IamResponse<List<ApplicationDTO>> getAllApplications() {
//
//        Long userId = apiUtils.getCurrentUserId();
//
//        List<ApplicationDTO> applications = applicationRepository
//                .findAllByCompany_Owner_Id(userId)
//                .stream()
//                .map(applicationMapper::toDto)
//                .toList();
//
//        return IamResponse.createSuccessful(applications);
//    }

    @Override
    public IamResponse<ApplicationDetailsDTO> getApplicationDetails(Long id) {

        Application application = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                ApiErrorMessage.NOT_FOUND_BY_ID.getMessage(id)
                        )
                );

        // Security: USER can only access own application.
        // ADMIN can access it according to your existing validator.
        accessValidator.validateAdminOrOwnerAccess(
                application.getCompany().getOwner().getId()
        );

        ApplicationDetailsDTO details = ApplicationDetailsDTO.builder()
                .id(application.getId())
                .company(companyMapper.toDTO(application.getCompany()))
                .report(reportMapper.toReportDto(application.getReport()))
                .application(applicationMapper.toDto(application))
                .build();

        return IamResponse.createSuccessful(details);
    }

    public IamResponse<List<ApplicationListDTO>> getAllApplications() {

        Long userId = apiUtils.getCurrentUserId();

        List<Application> applications =
                applicationRepository.findAllByCompany_Owner_Id(userId);

        List<ApplicationListDTO> result = applications.stream()
                .map(application -> {

                    Scoring scoring = application.getScoring();

                    AiAnalysis aiAnalysis = aiAnalysisRepository
                            .findByApplication_Id(application.getId())
                            .orElse(null);

                    return ApplicationListDTO.builder()
                            .id(application.getId())

                            .companyId(
                                    application.getCompany().getId()
                            )
                            .companyName(
                                    application.getCompany().getCompanyName()
                            )

                            .loanAmount(
                                    application.getLoanAmount()
                            )

                            .rating(
                                    scoring != null
                                            ? scoring.getRating()
                                            : null
                            )

                            .decision(
                                    scoring != null
                                            ? scoring.getDecision()
                                            : null
                            )

                            .riskLevel(
                                    aiAnalysis != null
                                            ? aiAnalysis.getRiskLevel()
                                            : null
                            )

                            .confidenceScore(
                                    aiAnalysis != null
                                            ? aiAnalysis.getConfidenceScore()
                                            : null
                            )

                            .createdAt(
                                    application.getCreatedAt()
                            )

                            .build();
                })
                .toList();

        return IamResponse.createSuccessful(result);
    }


}
