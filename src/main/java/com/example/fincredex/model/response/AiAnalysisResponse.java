package com.example.fincredex.model.response;

import com.example.fincredex.model.enums.RiskLevel;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiAnalysisResponse {

    private RiskLevel riskLevel;

    private String summary;

    private List<String> strengths;
    private List<String> weaknesses;

    private Map<String, String> metricAnalysis;
    private List<String> stopFactors;

    private String recommendation;
    private String reasoning;

    private Integer confidenceScore;
}