package com.example.fincredex.controller;

import com.example.fincredex.model.dto.ReportDTO;
import com.example.fincredex.model.entities.Report;
import com.example.fincredex.model.request.NewReportRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/create/companies/{companyId}/")
    public ResponseEntity<IamResponse<ReportDTO>> createReport(
            @RequestBody NewReportRequest request,
            @PathVariable(name = "companyId") Long companyId) {
        log.trace("create Monthly report: {}", request);

        IamResponse<ReportDTO> response = reportService.createReport(request,companyId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/id")
    public ResponseEntity<IamResponse<ReportDTO>> updateReport(
            @RequestBody NewReportRequest request,
            @PathVariable(name = "id") Long id) {
        log.trace("update Monthly report: {}", request);

        IamResponse<ReportDTO> response = reportService.updateReport(request, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<IamResponse<List<ReportDTO>>> getReportsByCompanyId(
            @PathVariable Long companyId
    ) {

        return ResponseEntity.ok(
                reportService.getReportsByCompanyId(companyId)
        );
    }
}
