package com.example.fincredex.service.impl;

import com.example.fincredex.exception.NotFoundException;
import com.example.fincredex.mapper.ScoringMapper;
import com.example.fincredex.model.Constants.ApiErrorMessage;
import com.example.fincredex.model.dto.ScoringDTO;
import com.example.fincredex.model.entities.Scoring;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.repository.ScoringRepository;
import com.example.fincredex.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {

    private final ScoringRepository scoringRepository;
    private final ScoringMapper scoringMapper;

    public IamResponse<ScoringDTO> getByApplicationId(Long applicationId) {

        Scoring scoring = scoringRepository
                .findByApplication_Id(applicationId)
                .orElseThrow(() ->
                        new NotFoundException(
                                ApiErrorMessage.NOT_FOUND_BY_ID
                                        .getMessage(applicationId)
                        )
                );

        ScoringDTO dto = scoringMapper.toDto(scoring);

        return IamResponse.createSuccessful(dto);
    }


}