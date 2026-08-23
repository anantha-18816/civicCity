package com.civicAI.backend.service;

import com.civicAI.backend.dto.AiInsights;
import com.civicAI.backend.dto.HeatPoint;
import com.civicAI.backend.dto.MapPoint;
import com.civicAI.backend.dto.ResolutionStats;
import com.civicAI.backend.entity.AiAnalysis;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.entity.ComplaintCluster;
import com.civicAI.backend.entity.Resolution;
import com.civicAI.backend.repository.AiAnalysisRepository;
import com.civicAI.backend.repository.ComplaintClusterRepository;
import com.civicAI.backend.repository.ComplaintRepository;
import com.civicAI.backend.repository.ResolutionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintClusterRepository clusterRepository;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final ResolutionRepository resolutionRepository;

    public DashboardService(ComplaintRepository complaintRepository,
                            ComplaintClusterRepository clusterRepository,
                            AiAnalysisRepository aiAnalysisRepository,
                            ResolutionRepository resolutionRepository) {
        this.complaintRepository = complaintRepository;
        this.clusterRepository = clusterRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.resolutionRepository = resolutionRepository;
    }

    public List<MapPoint> complaintMap() {
        Map<Long, AiAnalysis> analysisByComplaint = aiAnalysisRepository.findAll().stream()
                .collect(Collectors.toMap(AiAnalysis::getComplaintId, Function.identity()));

        return complaintRepository.findAll().stream()
                .map(complaint -> {
                    MapPoint point = new MapPoint();
                    point.setId(complaint.getId());
                    point.setIssueType(complaint.getIssueType());
                    point.setStatus(complaint.getStatus());
                    point.setPriority(complaint.getPriority());
                    point.setLatitude(complaint.getLocation().getY());
                    point.setLongitude(complaint.getLocation().getX());
                    Optional.ofNullable(analysisByComplaint.get(complaint.getId()))
                            .map(AiAnalysis::getSeverity)
                            .ifPresent(point::setSeverity);
                    return point;
                })
                .toList();
    }

    public List<HeatPoint> heatmap() {
        List<HeatPoint> points = clusterRepository.findAll().stream()
                .map(this::toHeatPoint)
                .collect(Collectors.toList());

        complaintRepository.findAll().stream()
                .filter(c -> c.getDuplicateOfId() != null)
                .filter(c -> c.getClusterId() == null)
                .forEach(c -> points.add(toOrphanHeatPoint(c)));

        return points;
    }

    public ResolutionStats resolutionStats() {
        List<Resolution> resolutions = resolutionRepository.findAll();
        Map<Long, Complaint> complaintById = complaintRepository.findAll().stream()
                .collect(Collectors.toMap(Complaint::getId, Function.identity()));

        double avgHours = resolutions.stream()
                .map(r -> resolutionHours(r, complaintById.get(r.getComplaintId())))
                .filter(h -> h != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        ResolutionStats stats = new ResolutionStats();
        stats.setTotalResolved(resolutions.size());
        stats.setVerified(resolutions.stream().filter(r -> "VERIFIED".equals(r.getVerificationStatus())).count());
        stats.setRejected(resolutions.stream().filter(r -> "REJECTED".equals(r.getVerificationStatus())).count());
        stats.setPendingVerification(resolutions.stream().filter(r -> "PENDING".equals(r.getVerificationStatus())).count());
        stats.setAvgResolutionHours(Math.round(avgHours * 100.0) / 100.0);
        return stats;
    }

    public AiInsights aiInsights() {
        List<AiAnalysis> analyses = aiAnalysisRepository.findAll();

        AiInsights insights = new AiInsights();
        insights.setTotalAnalyses(analyses.size());
        insights.setDetectionsByObject(analyses.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDetectedObject() != null ? a.getDetectedObject() : "UNKNOWN",
                        Collectors.counting())));
        insights.setComplaintsBySeverity(analyses.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getSeverity() != null ? a.getSeverity() : "UNRATED",
                        Collectors.counting())));

        insights.setAvgConfidence(analyses.stream()
                .filter(a -> a.getConfidence() != null)
                .mapToDouble(AiAnalysis::getConfidence)
                .average()
                .orElse(0.0));

        insights.setDuplicatesLinked(complaintRepository.countByDuplicateOfIdNotNull());

        insights.setLatestModelVersion(analyses.stream()
                .map(AiAnalysis::getModelVersion)
                .filter(v -> v != null)
                .max(Comparator.naturalOrder())
                .orElse(null));

        return insights;
    }

    private HeatPoint toHeatPoint(ComplaintCluster cluster) {
        HeatPoint point = new HeatPoint();
        point.setLatitude(cluster.getLocation().getY());
        point.setLongitude(cluster.getLocation().getX());
        point.setWeight(cluster.getReportCount());
        point.setSeverity(cluster.getSeverity());
        return point;
    }

    private HeatPoint toOrphanHeatPoint(Complaint complaint) {
        HeatPoint point = new HeatPoint();
        point.setLatitude(complaint.getLocation().getY());
        point.setLongitude(complaint.getLocation().getX());
        point.setWeight(1);
        point.setSeverity(null);
        return point;
    }

    private Double resolutionHours(Resolution resolution, Complaint complaint) {
        if (complaint == null || complaint.getCreatedAt() == null
                || resolution.getResolvedAt() == null) {
            return null;
        }
        return Duration.between(complaint.getCreatedAt(), resolution.getResolvedAt()).toMillis()
                / 3_600_000.0;
    }
}
