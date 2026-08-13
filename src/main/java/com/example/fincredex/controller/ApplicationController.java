package com.example.fincredex.controller;

import com.example.fincredex.model.dto.*;
import com.example.fincredex.model.entities.Company;
import com.example.fincredex.model.request.NewApplicationRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.service.ApplicationService;
import com.example.fincredex.service.ScoringService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequestMapping("/application")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ScoringService scoringService;

    @PostMapping("/create/reports/{reportId}")
    public ResponseEntity<IamResponse<ApplicationDTO>> createApplication(
            @RequestBody @Valid NewApplicationRequest request,
            @PathVariable Long reportId
    ) {
        IamResponse<ApplicationDTO> response = applicationService
                .createApplication(request, reportId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{applicationId}/scoring")
    public ResponseEntity<IamResponse<ScoringDTO>> getScoring(
            @PathVariable Long applicationId
    ) {

        return ResponseEntity.ok(
                scoringService.getByApplicationId(applicationId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<IamResponse<ApplicationDTO>> updateApplication(
            @RequestBody NewApplicationRequest request,
            @PathVariable(name = "id") Long id
    ){

        IamResponse<ApplicationDTO> response = applicationService.updateApplication(request, id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/id")
    public ResponseEntity<IamResponse<ApplicationDTO>> deleteApplication(
            @PathVariable Long id
    ){
        IamResponse<ApplicationDTO> response = applicationService.deleteApplication(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IamResponse<ApplicationDTO>> getApplicationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationById(id)
        );
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<IamResponse<ApplicationDetailsDTO>> getApplicationDetails(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                applicationService.getApplicationDetails(id)
        );
    }

    @GetMapping
    public ResponseEntity<IamResponse<List<ApplicationListDTO>>> getAllApplications() {

        return ResponseEntity.ok(
                applicationService.getAllApplications()
        );
    }
}
