package com.example.fincredex.controller;

import com.example.fincredex.model.dto.CompanyDTO;
import com.example.fincredex.model.request.CompanyRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/create")
    public ResponseEntity<IamResponse<CompanyDTO>> createCompany(
            @RequestBody CompanyRequest request,
            Principal principal) {
        log.trace("create company request: {}", request);

        IamResponse<CompanyDTO> response = companyService.createCompany(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<IamResponse<List<CompanyDTO>>> getAllCompanies() {

        return ResponseEntity.ok(
                companyService.getAllCompanies()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<IamResponse<CompanyDTO>> getCompanyById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                companyService.getCompanyById(id)
        );
    }
}
