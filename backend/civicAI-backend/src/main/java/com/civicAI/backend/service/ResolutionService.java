package com.civicAI.backend.service;

import com.civicAI.backend.dto.ResolutionRequest;
import com.civicAI.backend.dto.ResolutionResponse;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.entity.Resolution;
import com.civicAI.backend.exception.ResourceNotFoundException;
import com.civicAI.backend.exception.InvalidStateException;
import com.civicAI.backend.repository.ComplaintRepository;
import com.civicAI.backend.repository.ResolutionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class ResolutionService {

    private static final Set<String> RESOLVABLE_STATUSES = Set.of("ASSIGNED", "IN_PROGRESS");
    private static final Set<String> VERIFICATION_STATUSES = Set.of("VERIFIED", "REJECTED");

    private final ResolutionRepository resolutionRepository;
    private final ComplaintRepository complaintRepository;

    public ResolutionService(ResolutionRepository resolutionRepository,
                             ComplaintRepository complaintRepository) {
        this.resolutionRepository = resolutionRepository;
        this.complaintRepository = complaintRepository;
    }

    public ResolutionResponse submitResolution(Long complaintId, ResolutionRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Complaint not found with id: " + complaintId));

        if (!RESOLVABLE_STATUSES.contains(complaint.getStatus())) {
            throw new InvalidStateException("Complaint " + complaintId
                    + " cannot be resolved from status '" + complaint.getStatus() + "'");
        }

        if (resolutionRepository.findByComplaintId(complaintId).isPresent()) {
            throw new InvalidStateException(
                    "Complaint " + complaintId + " already has a resolution record");
        }

        OffsetDateTime now = OffsetDateTime.now();

        Resolution resolution = new Resolution();
        resolution.setComplaintId(complaintId);
        resolution.setOfficerId(request.getOfficerId());
        resolution.setBeforeImageUrl(request.getBeforeImageUrl());
        resolution.setAfterImageUrl(request.getAfterImageUrl());
        resolution.setNotes(request.getNotes());
        resolution.setResolvedAt(now);

        complaint.setStatus("RESOLVED");
        complaint.setResolvedAt(now);
        complaintRepository.save(complaint);

        return toResponse(resolutionRepository.save(resolution), complaint);
    }

    public ResolutionResponse verify(Long resolutionId, String verificationStatus) {
        String status = verificationStatus.toUpperCase();
        if (!VERIFICATION_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid verification status '" + verificationStatus
                            + "'. Allowed values: " + VERIFICATION_STATUSES);
        }

        Resolution resolution = resolutionRepository.findById(resolutionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resolution not found with id: " + resolutionId));

        resolution.setVerificationStatus(status);
        resolution.setVerifiedAt(OffsetDateTime.now());

        Complaint complaint = complaintRepository.findById(resolution.getComplaintId())
                .orElseThrow(() -> new IllegalStateException(
                        "Complaint " + resolution.getComplaintId()
                                + " referenced by resolution no longer exists"));

        return toResponse(resolutionRepository.save(resolution), complaint);
    }

    public ResolutionResponse getByComplaintId(Long complaintId) {
        Resolution resolution = resolutionRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No resolution found for complaint id: " + complaintId));
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        return toResponse(resolution, complaint);
    }

    private ResolutionResponse toResponse(Resolution resolution, Complaint complaint) {
        ResolutionResponse response = new ResolutionResponse();
        response.setId(resolution.getId());
        response.setComplaintId(resolution.getComplaintId());
        response.setOfficerId(resolution.getOfficerId());
        response.setBeforeImageUrl(resolution.getBeforeImageUrl());
        response.setAfterImageUrl(resolution.getAfterImageUrl());
        response.setVerificationStatus(resolution.getVerificationStatus());
        response.setNotes(resolution.getNotes());
        response.setResolvedAt(resolution.getResolvedAt());
        response.setVerifiedAt(resolution.getVerifiedAt());

        if (complaint != null && complaint.getCreatedAt() != null
                && resolution.getResolvedAt() != null) {
            double hours = Duration.between(
                    complaint.getCreatedAt(), resolution.getResolvedAt()).toMillis() / 3_600_000.0;
            response.setResolutionTimeHours(Math.round(hours * 100.0) / 100.0);
        }

        return response;
    }
}

