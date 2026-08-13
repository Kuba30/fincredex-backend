package com.example.fincredex.service;

import com.example.fincredex.model.dto.ScoringDTO;
import com.example.fincredex.model.entities.Application;
import com.example.fincredex.model.entities.Scoring;
import com.example.fincredex.model.response.IamResponse;

public interface ScoringService {

    public IamResponse<ScoringDTO> getByApplicationId(Long applicationId);

}
