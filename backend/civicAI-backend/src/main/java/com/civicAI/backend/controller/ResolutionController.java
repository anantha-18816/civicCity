package com.civicAI.backend.controller;

import com.civicAI.backend.dto.ResolutionRequest;
import com.civicAI.backend.dto.ResolutionResponse;
import com.civicAI.backend.dto.VerificationRequest;
import com.civicAI.backend.service.ResolutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ResolutionController {

    private final ResolutionService resolutionService;

    public ResolutionController(ResolutionService resolutionService) {
        this.resolutionService = resolutionService;
    }

@PostMapping("/complaints/{id}/resolution")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OFFICER')")
    public ResolutionResponse submit(@PathVariable Long id,
                                     @Valid @RequestBody ResolutionRequest request) {
        return resolutionService.submitResolution(id, request);
    }

    @GetMapping("/complaints/{id}/resolution")
    public ResolutionResponse getByComplaint(@PathVariable Long id) {
        return resolutionService.getByComplaintId(id);
    }

@PostMapping("/resolutions/{id}/verify")
    @PreAuthorize("hasRole('OFFICER')")
    public ResolutionResponse verify(@PathVariable Long id,
                                     @Valid @RequestBody VerificationRequest request) {
        return resolutionService.verify(id, request.getVerificationStatus());
    }
}

