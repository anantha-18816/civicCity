package com.civicAI.backend.service;

import com.civicAI.backend.dto.AiAnalysisResponse;
import com.civicAI.backend.dto.AiAnalysisResult;
import com.civicAI.backend.entity.AiAnalysis;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.exception.ResourceNotFoundException;
import com.civicAI.backend.repository.AiAnalysisRepository;
import com.civicAI.backend.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;

@Service
public class AiAnalysisService {

    private final AiAnalysisRepository aiAnalysisRepository;
    private final ComplaintRepository complaintRepository;
    private final DuplicateDetectionService duplicateDetectionService;
    private final ClusterService clusterService;
    private final PriorityService priorityService;
    private final RestClient restClient;

    public AiAnalysisService(AiAnalysisRepository aiAnalysisRepository,
                             ComplaintRepository complaintRepository,
                             DuplicateDetectionService duplicateDetectionService,
                             ClusterService clusterService,
                             PriorityService priorityService,
                             @Value("${ai-service.url}") String aiServiceUrl) {
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.complaintRepository = complaintRepository;
        this.duplicateDetectionService = duplicateDetectionService;
        this.clusterService = clusterService;
        this.priorityService = priorityService;
        this.restClient = RestClient.builder()
                .baseUrl(aiServiceUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    public AiAnalysisResponse analyzeComplaint(Long complaintId, MultipartFile image) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        AiAnalysisResult result;
        try {
            result = restClient.post()
                    .uri("/api/analyze")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(image))
                    .retrieve()
                    .body(AiAnalysisResult.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("AI analysis service is unavailable", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploaded image", e);
        }

        AiAnalysis entity = aiAnalysisRepository.findByComplaintId(complaintId)
                .orElseGet(AiAnalysis::new);
        entity.setComplaintId(complaintId);
        entity.setDetectedObject(result.getDetectedObject());
        entity.setSeverity(result.getSeverity());
        entity.setConfidence(result.getConfidence());
        entity.setEstimatedLengthM(result.getEstimatedLengthM());
        entity.setEstimatedWidthM(result.getEstimatedWidthM());
        entity.setRoadRisk(result.getRoadRisk());
        entity.setModelVersion(result.getModelVersion());
        entity.setImageHash(result.getImageHash());
        entity.setAnalyzedAt(OffsetDateTime.now());

        AiAnalysis saved = aiAnalysisRepository.save(entity);

        complaint.setStatus("AI_ANALYZED");
        complaintRepository.save(complaint);

        if (!"NO_ISSUE".equals(result.getDetectedObject())) {
            duplicateDetectionService.detectDuplicate(complaint, saved.getImageHash());
        }

        clusterService.assignCluster(complaint);

        PriorityService.ScoredPriority scored = priorityService.compute(complaint);
        complaint.setPriority(scored.priority());
        complaintRepository.save(complaint);

        return toResponse(saved);
    }

    public AiAnalysisResponse getAnalysisForComplaint(Long complaintId) {
        AiAnalysis analysis = aiAnalysisRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No AI analysis found for complaint id: " + complaintId));
        return toResponse(analysis);
    }

    private MultiValueMap<String, Object> toMultipart(MultipartFile image) throws IOException {
        String filename = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new HttpEntity<>(new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        }));
        return body;
    }

    private AiAnalysisResponse toResponse(AiAnalysis analysis) {
        AiAnalysisResponse response = new AiAnalysisResponse();

        response.setId(analysis.getId());
        response.setComplaintId(analysis.getComplaintId());
        response.setDetectedObject(analysis.getDetectedObject());
        response.setSeverity(analysis.getSeverity());
        response.setConfidence(analysis.getConfidence());
        response.setEstimatedLengthM(analysis.getEstimatedLengthM());
        response.setEstimatedWidthM(analysis.getEstimatedWidthM());
        response.setRoadRisk(analysis.getRoadRisk());
        response.setModelVersion(analysis.getModelVersion());
        response.setImageHash(analysis.getImageHash());
        response.setAnalyzedAt(analysis.getAnalyzedAt());

        return response;
    }
}