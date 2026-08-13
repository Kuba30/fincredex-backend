package com.example.fincredex.service.impl;

import com.example.fincredex.exception.NotFoundException;
import com.example.fincredex.mapper.CompanyMapper;
import com.example.fincredex.mapper.UserMapper;
import com.example.fincredex.model.Constants.ApiErrorMessage;
import com.example.fincredex.model.dto.ApplicationDTO;
import com.example.fincredex.model.dto.CompanyDTO;
import com.example.fincredex.model.entities.Application;
import com.example.fincredex.model.entities.Company;
import com.example.fincredex.model.entities.Report;
import com.example.fincredex.model.entities.User;
import com.example.fincredex.model.request.CompanyRequest;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.repository.CompanyRepository;
import com.example.fincredex.repository.UserRepository;
import com.example.fincredex.security.validation.AccessValidator;
import com.example.fincredex.service.CompanyService;
import com.example.fincredex.utils.ApiUtils;
import jakarta.persistence.Access;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final AccessValidator accessValidator;
    private final ApiUtils apiUtils;


    @Override
    public IamResponse<CompanyDTO> createCompany(CompanyRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        Company company = companyMapper.create(request);
        company.setOwner(user);

        Company saved = companyRepository.save(company);
        CompanyDTO companyDTO = companyMapper.toDTO(saved);

        return IamResponse.success(companyDTO);
    }

    public IamResponse<List<CompanyDTO>> getAllCompanies() {

        Long userId = apiUtils.getCurrentUserId();

        List<CompanyDTO> companies = companyRepository
                .findAllByOwner_Id(userId)
                .stream()
                .map(companyMapper::toDTO)
                .toList();

        return IamResponse.createSuccessful(companies);
    }

    public IamResponse<CompanyDTO> getCompanyById(Long id) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                ApiErrorMessage.NOT_FOUND_BY_ID.getMessage(id)
                        )
                );

        accessValidator.validateAdminOrOwnerAccess(
                company.getOwner().getId()
        );

        CompanyDTO companyDTO = companyMapper.toDTO(company);

        return IamResponse.createSuccessful(companyDTO);
    }
}
