package com.civicAI.backend.controller;

import com.civicAI.backend.dto.AiAnalysisResponse;
import com.civicAI.backend.service.AiAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/complaints")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping(value = "/{id}/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiAnalysisResponse analyzeComplaint(@PathVariable Long id,
                                               @RequestPart("image") MultipartFile image) {
        return aiAnalysisService.analyzeComplaint(id, image);
    }

    @GetMapping("/{id}/analysis")
    public AiAnalysisResponse getAnalysis(@PathVariable Long id) {
        return aiAnalysisService.getAnalysisForComplaint(id);
    }
}