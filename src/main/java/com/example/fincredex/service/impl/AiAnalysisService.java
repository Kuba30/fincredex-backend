package com.example.fincredex.service.impl;

import com.example.fincredex.model.entities.AiAnalysis;
import com.example.fincredex.model.entities.Application;
import com.example.fincredex.model.entities.Company;
import com.example.fincredex.model.entities.Report;
import com.example.fincredex.model.entities.Scoring;
import com.example.fincredex.model.response.AiAnalysisResponse;
import com.example.fincredex.repository.AiAnalysisRepository;
import com.example.fincredex.repository.ApplicationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final ApplicationRepository applicationRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private static final int MAX_AI_ATTEMPTS = 3;

    private static final String SYSTEM_PROMPT = """
            You are a senior bank credit analyst.

            Analyze application strictly.

            IMPORTANT:
            Return ONLY valid JSON.
            Do NOT wrap in ```json``` or any markdown.
            Do NOT add explanatory text before or after.
            Start directly with { and end with }.

            FIELD CONSTRAINTS:
            - riskLevel MUST be exactly one of:
              HIGH, MEDIUM, LOW.
            - confidenceScore MUST be an integer between 0 and 100.
            - strengths MUST be an array of strings.
            - weaknesses MUST be an array of strings.
            - stopFactors MUST be an array of strings.
            - metricAnalysis MUST be a JSON object.
            - recommendation MUST be a string.
            - reasoning MUST be a string.

            Return format:
            {
              "riskLevel": "",
              "summary": "",
              "strengths": [],
              "weaknesses": [],
              "metricAnalysis": {
                  "revenueAssessment": "",
                  "debtLoadAssessment": "",
                  "dscrAssessment": "",
                  "paymentHistoryAssessment": "",
                  "businessAgeAssessment": ""
              },
              "stopFactors": [],
              "recommendation": "",
              "reasoning": "",
              "confidenceScore": 0
            }
            """;

    // =========================================================
    // ANALYZE
    // =========================================================

    public AiAnalysisResponse analyze(Long applicationId) {

        // ---------------------------------------------------------
        // 1. Return already existing analysis
        // ---------------------------------------------------------

        var existing =
                aiAnalysisRepository
                        .findByApplication_Id(applicationId);

        if (existing.isPresent()) {

            log.info(
                    "AI analysis already exists for application {}",
                    applicationId
            );

            return convertToResponse(
                    existing.get()
            );
        }

        // ---------------------------------------------------------
        // 2. Load application
        // ---------------------------------------------------------

        Application app =
                applicationRepository
                        .findById(applicationId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Application not found: "
                                                + applicationId
                                )
                        );

        Company company =
                app.getCompany();

        Report report =
                app.getReport();

        Scoring scoring =
                app.getScoring();

        if (company == null) {
            throw new RuntimeException(
                    "Company data is missing for this application."
            );
        }

        if (report == null) {
            throw new RuntimeException(
                    "Financial report is missing for this application."
            );
        }

        if (scoring == null) {
            throw new RuntimeException(
                    "Scoring must be calculated before AI analysis."
            );
        }

        String prompt =
                buildPrompt(
                        company,
                        report,
                        app,
                        scoring
                );

        // ---------------------------------------------------------
        // 3. Retry only AI generation + JSON parsing
        // ---------------------------------------------------------

        AiAnalysisResponse response = null;

        String successfulRawJson = null;

        Exception lastException = null;

        for (
                int attempt = 1;
                attempt <= MAX_AI_ATTEMPTS;
                attempt++
        ) {

            try {

                log.info(
                        "AI analysis attempt {}/{} for application {}",
                        attempt,
                        MAX_AI_ATTEMPTS,
                        applicationId
                );

                // -------------------------------------------------
                // Call Anthropic
                // -------------------------------------------------

                String rawJson =
                        chatClient
                                .prompt()
                                .system(SYSTEM_PROMPT)
                                .user(prompt)
                                .options(
                                        AnthropicChatOptions
                                                .builder()
                                                .maxTokens(2000)
                                                .thinking(
                                                        AnthropicApi
                                                                .ThinkingType
                                                                .DISABLED,
                                                        null
                                                )
                                                .build()
                                )
                                .call()
                                .content();

                log.info(
                        "Raw AI response attempt {}: [{}]",
                        attempt,
                        rawJson
                );

                // -------------------------------------------------
                // Clean possible markdown/noise
                // -------------------------------------------------

                String cleanedJson =
                        cleanJson(rawJson);

                log.debug(
                        "Cleaned AI JSON attempt {}: [{}]",
                        attempt,
                        cleanedJson
                );

                // -------------------------------------------------
                // Parse JSON
                // -------------------------------------------------

                AiAnalysisResponse parsed =
                        objectMapper.readValue(
                                cleanedJson,
                                AiAnalysisResponse.class
                        );

                // -------------------------------------------------
                // Validate result
                // -------------------------------------------------

                validateResponse(parsed);

                response =
                        parsed;

                successfulRawJson =
                        rawJson;

                log.info(
                        "AI response successfully parsed on attempt {} for application {}",
                        attempt,
                        applicationId
                );

                break;

            } catch (Exception e) {

                lastException =
                        e;

                log.warn(
                        "AI generation/parsing attempt {}/{} failed for application {}: {}",
                        attempt,
                        MAX_AI_ATTEMPTS,
                        applicationId,
                        e.getMessage()
                );
            }
        }

        // ---------------------------------------------------------
        // 4. All attempts failed
        // ---------------------------------------------------------

        if (response == null) {

            throw new RuntimeException(
                    "AI analysis could not be generated after "
                            + MAX_AI_ATTEMPTS
                            + " attempts. Please try again.",
                    lastException
            );
        }

        // ---------------------------------------------------------
        // 5. Check again before saving
        //
        // Another request may have saved the result while
        // Anthropic was processing.
        // ---------------------------------------------------------

        var existingAfterGeneration =
                aiAnalysisRepository
                        .findByApplication_Id(applicationId);

        if (existingAfterGeneration.isPresent()) {

            log.info(
                    "AI analysis for application {} was already saved by another request.",
                    applicationId
            );

            return convertToResponse(
                    existingAfterGeneration.get()
            );
        }

        // ---------------------------------------------------------
        // 6. Save analysis
        // ---------------------------------------------------------

        try {

            AiAnalysis entity =
                    AiAnalysis.builder()
                            .application(app)

                            .riskLevel(
                                    response.getRiskLevel()
                            )

                            .summary(
                                    response.getSummary()
                            )

                            .strengths(
                                    objectMapper.writeValueAsString(
                                            response.getStrengths()
                                    )
                            )

                            .weaknesses(
                                    objectMapper.writeValueAsString(
                                            response.getWeaknesses()
                                    )
                            )

                            .metricAnalysis(
                                    objectMapper.writeValueAsString(
                                            response.getMetricAnalysis()
                                    )
                            )

                            .stopFactors(
                                    objectMapper.writeValueAsString(
                                            response.getStopFactors()
                                    )
                            )

                            .recommendation(
                                    response.getRecommendation()
                            )

                            .reasoning(
                                    response.getReasoning()
                            )

                            .confidenceScore(
                                    response.getConfidenceScore()
                            )

                            .rawResponse(
                                    successfulRawJson
                            )

                            .createdAt(
                                    LocalDateTime.now()
                            )

                            .build();

            aiAnalysisRepository
                    .saveAndFlush(entity);

            log.info(
                    "AI analysis saved successfully for application {}",
                    applicationId
            );

            return response;

        } catch (DataIntegrityViolationException e) {

            /*
             * Two requests may still reach INSERT almost
             * simultaneously.
             *
             * One succeeds.
             * The second one hits the unique constraint.
             *
             * Do NOT retry Anthropic.
             * Just return the existing record.
             */

            log.warn(
                    "Duplicate AI analysis insert for application {}. Returning existing analysis.",
                    applicationId
            );

            return aiAnalysisRepository
                    .findByApplication_Id(applicationId)

                    .map(
                            this::convertToResponse
                    )

                    .orElseThrow(() ->
                            new RuntimeException(
                                    "AI analysis was generated but could not be saved.",
                                    e
                            )
                    );

        } catch (Exception e) {

            throw new RuntimeException(
                    "AI analysis was generated but could not be saved.",
                    e
            );
        }
    }

    // =========================================================
    // CLEAN JSON
    // =========================================================

    private String cleanJson(
            String rawJson
    ) {

        if (
                rawJson == null
                        ||
                        rawJson.isBlank()
        ) {

            throw new RuntimeException(
                    "AI returned an empty response."
            );
        }

        String cleaned =
                rawJson.trim();

        // Sometimes model can still return:
        //
        // ```json
        // {...}
        // ```

        if (
                cleaned.startsWith(
                        "```json"
                )
        ) {

            cleaned =
                    cleaned.substring(7);

        } else if (
                cleaned.startsWith(
                        "```"
                )
        ) {

            cleaned =
                    cleaned.substring(3);
        }

        if (
                cleaned.endsWith(
                        "```"
                )
        ) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 3
                    );
        }

        cleaned =
                cleaned.trim();

        // If model added some text,
        // take only first { ... last }

        int firstBrace =
                cleaned.indexOf('{');

        int lastBrace =
                cleaned.lastIndexOf('}');

        if (
                firstBrace >= 0
                        &&
                        lastBrace > firstBrace
        ) {

            cleaned =
                    cleaned.substring(
                            firstBrace,
                            lastBrace + 1
                    );
        }

        return cleaned.trim();
    }

    // =========================================================
    // VALIDATE AI RESPONSE
    // =========================================================

    private void validateResponse(
            AiAnalysisResponse response
    ) {

        if (response == null) {

            throw new RuntimeException(
                    "AI returned an empty analysis."
            );
        }

        // ---------------------------------------------------------
        // Risk Level
        // ---------------------------------------------------------

        if (
                response.getRiskLevel() == null
        ) {

            throw new RuntimeException(
                    "AI response is missing riskLevel."
            );
        }

        String riskLevel =
                response
                        .getRiskLevel()
                        .toString();

        if (
                !riskLevel.equals("LOW")
                        &&
                        !riskLevel.equals("MEDIUM")
                        &&
                        !riskLevel.equals("HIGH")
        ) {

            throw new RuntimeException(
                    "AI returned invalid riskLevel: "
                            + riskLevel
            );
        }

        // ---------------------------------------------------------
        // Summary
        // ---------------------------------------------------------

        if (
                response.getSummary() == null
                        ||
                        response
                                .getSummary()
                                .isBlank()
        ) {

            throw new RuntimeException(
                    "AI response is missing summary."
            );
        }

        // ---------------------------------------------------------
        // Recommendation
        // ---------------------------------------------------------

        if (
                response.getRecommendation() == null
                        ||
                        response
                                .getRecommendation()
                                .isBlank()
        ) {

            throw new RuntimeException(
                    "AI response is missing recommendation."
            );
        }

        // ---------------------------------------------------------
        // Reasoning
        // ---------------------------------------------------------

        if (
                response.getReasoning() == null
                        ||
                        response
                                .getReasoning()
                                .isBlank()
        ) {

            throw new RuntimeException(
                    "AI response is missing reasoning."
            );
        }

        // ---------------------------------------------------------
        // Confidence
        // ---------------------------------------------------------

        if (
                response.getConfidenceScore() == null
                        ||
                        response.getConfidenceScore() < 0
                        ||
                        response.getConfidenceScore() > 100
        ) {

            throw new RuntimeException(
                    "AI returned invalid confidence score."
            );
        }

        // ---------------------------------------------------------
        // Collections
        // ---------------------------------------------------------

        if (
                response.getStrengths() == null
        ) {

            throw new RuntimeException(
                    "AI response is missing strengths."
            );
        }

        if (
                response.getWeaknesses() == null
        ) {

            throw new RuntimeException(
                    "AI response is missing weaknesses."
            );
        }

        if (
                response.getMetricAnalysis() == null
        ) {

            throw new RuntimeException(
                    "AI response is missing metricAnalysis."
            );
        }

        if (
                response.getStopFactors() == null
        ) {

            throw new RuntimeException(
                    "AI response is missing stopFactors."
            );
        }
    }

    // =========================================================
    // CONVERT DATABASE ENTITY -> RESPONSE
    // =========================================================

    private AiAnalysisResponse convertToResponse(
            AiAnalysis entity
    ) {

        try {

            AiAnalysisResponse response =
                    new AiAnalysisResponse();

            response.setRiskLevel(
                    entity.getRiskLevel()
            );

            response.setSummary(
                    entity.getSummary()
            );

            // -----------------------------------------------------
            // strengths JSON -> List<String>
            // -----------------------------------------------------

            List<String> strengths =
                    objectMapper.readValue(
                            entity.getStrengths(),

                            objectMapper
                                    .getTypeFactory()
                                    .constructCollectionType(
                                            List.class,
                                            String.class
                                    )
                    );

            response.setStrengths(
                    strengths
            );

            // -----------------------------------------------------
            // weaknesses JSON -> List<String>
            // -----------------------------------------------------

            List<String> weaknesses =
                    objectMapper.readValue(
                            entity.getWeaknesses(),

                            objectMapper
                                    .getTypeFactory()
                                    .constructCollectionType(
                                            List.class,
                                            String.class
                                    )
                    );

            response.setWeaknesses(
                    weaknesses
            );

            // -----------------------------------------------------
            // metricAnalysis JSON -> Map<String, String>
            // -----------------------------------------------------

            Map<String, String> metricAnalysis =
                    objectMapper.readValue(
                            entity.getMetricAnalysis(),

                            objectMapper
                                    .getTypeFactory()
                                    .constructMapType(
                                            Map.class,
                                            String.class,
                                            String.class
                                    )
                    );

            response.setMetricAnalysis(
                    metricAnalysis
            );

            // -----------------------------------------------------
            // stopFactors JSON -> List<String>
            // -----------------------------------------------------

            List<String> stopFactors =
                    objectMapper.readValue(
                            entity.getStopFactors(),

                            objectMapper
                                    .getTypeFactory()
                                    .constructCollectionType(
                                            List.class,
                                            String.class
                                    )
                    );

            response.setStopFactors(
                    stopFactors
            );

            response.setRecommendation(
                    entity.getRecommendation()
            );

            response.setReasoning(
                    entity.getReasoning()
            );

            response.setConfidenceScore(
                    entity.getConfidenceScore()
            );

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to read stored AI analysis.",
                    e
            );
        }
    }

    // =========================================================
    // BUILD PROMPT
    // =========================================================

    private String buildPrompt(
            Company company,
            Report report,
            Application application,
            Scoring scoring
    ) {

        return """
                COMPANY:
                Name: %s
                Industry: %s
                Age: %d

                REPORT:
                Revenue: %s
                Current payments: %s
                Late payments: %d

                APPLICATION:
                Loan amount: %s
                Interest rate: %s
                Term months: %d

                SCORING:
                New payment: %s
                Debt load: %s
                DSCR: %s
                Rating: %s
                Decision: %s

                Analyze the application.

                Return valid JSON only.
                """
                .formatted(
                        company.getCompanyName(),
                        company.getIndustry(),
                        company.getCompanyAgeYears(),

                        report.getMonthlyRevenue(),
                        report.getCurrentPayments(),
                        report.getLatePaymentsCount(),

                        application.getLoanAmount(),
                        application.getInterestRate(),
                        application.getTermMonths(),

                        scoring.getNewPayment(),
                        scoring.getDebtLoad(),
                        scoring.getDscr(),
                        scoring.getRating(),
                        scoring.getDecision()
                );
    }

    // =========================================================
    // DELETE OLD ANALYSIS
    // =========================================================

    @Transactional
    public void deleteAnalyze(
            Long applicationId
    ) {

        aiAnalysisRepository
                .deleteByApplication_Id(
                        applicationId
                );
    }
}