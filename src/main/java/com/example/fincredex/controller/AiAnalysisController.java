package com.example.fincredex.controller;

import com.example.fincredex.mapper.aiAnalysisMapper;
import com.example.fincredex.model.entities.AiAnalysis;
import com.example.fincredex.model.response.AiAnalysisResponse;
import com.example.fincredex.repository.AiAnalysisRepository;
import com.example.fincredex.service.impl.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final aiAnalysisMapper aiAnalysisMapper;

    @GetMapping("/{applicationId}/analyze")
    public ResponseEntity<AiAnalysisResponse> analyze(
            @PathVariable Long applicationId
    ) {

        Optional<AiAnalysis> existing =
                aiAnalysisRepository.findByApplication_Id(applicationId);

        if (existing.isPresent()) {
            return ResponseEntity.ok(
                    aiAnalysisMapper.toResponse(existing.get())
            );
        }

        return ResponseEntity.ok(
                aiAnalysisService.analyze(applicationId)
        );
    }
}