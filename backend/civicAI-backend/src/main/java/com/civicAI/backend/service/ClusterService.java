package com.civicAI.backend.service;

import com.civicAI.backend.entity.AiAnalysis;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.entity.ComplaintCluster;
import com.civicAI.backend.repository.AiAnalysisRepository;
import com.civicAI.backend.repository.ComplaintClusterRepository;
import com.civicAI.backend.repository.ComplaintRepository;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClusterService {

    private static final Logger log = LoggerFactory.getLogger(ClusterService.class);

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private static final Map<String, Integer> SEVERITY_RANK =
            Map.of("LOW", 1, "MEDIUM", 2, "HIGH", 3);

    private static final Map<Integer, String> RANK_SEVERITY =
            Map.of(1, "LOW", 2, "MEDIUM", 3, "HIGH");

    private static final Map<String, Integer> SEVERITY_PRIORITY =
            Map.of("HIGH", 1, "MEDIUM", 2, "LOW", 3);

    private final ComplaintRepository complaintRepository;
    private final ComplaintClusterRepository clusterRepository;
    private final AiAnalysisRepository aiAnalysisRepository;

    private final double radiusM;
    private final int denseReportThreshold;

    public ClusterService(ComplaintRepository complaintRepository,
                          ComplaintClusterRepository clusterRepository,
                          AiAnalysisRepository aiAnalysisRepository,
                          @Value("${cluster.radius-m:150}") double radiusM,
                          @Value("${cluster.dense-report-threshold:5}") int denseReportThreshold) {
        this.complaintRepository = complaintRepository;
        this.clusterRepository = clusterRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.radiusM = radiusM;
        this.denseReportThreshold = denseReportThreshold;
    }

    public void assignCluster(Complaint complaint) {
        ComplaintCluster cluster = resolveTargetCluster(complaint)
                .orElseGet(() -> createCluster(complaint));

        if (complaint.getClusterId() == null
                || !complaint.getClusterId().equals(cluster.getId())) {
            cluster.setReportCount(cluster.getReportCount() + 1);
        }

        complaint.setClusterId(cluster.getId());
        complaintRepository.save(complaint);

        recomputeRollups(cluster);

        log.info("Complaint {} assigned to cluster {} (reports={}, severity={}, priority={})",
                complaint.getId(), cluster.getId(), cluster.getReportCount(),
                cluster.getSeverity(), cluster.getPriority());
    }

    private Optional<ComplaintCluster> resolveTargetCluster(Complaint complaint) {
        if (complaint.getDuplicateOfId() != null) {
            Optional<Complaint> original = complaintRepository.findById(complaint.getDuplicateOfId());
            if (original.isPresent() && original.get().getClusterId() != null) {
                return clusterRepository.findById(original.get().getClusterId());
            }
        }

        return clusterRepository.findNearest(
                complaint.getIssueType(),
                complaint.getLocation().getY(),
                complaint.getLocation().getX(),
                radiusM)
                .stream()
                .findFirst();
    }

    private ComplaintCluster createCluster(Complaint complaint) {
        ComplaintCluster cluster = new ComplaintCluster();
        cluster.setIssueType(complaint.getIssueType());
        cluster.setLocation(GEOMETRY_FACTORY.createPoint(
                new org.locationtech.jts.geom.Coordinate(
                        complaint.getLocation().getX(),
                        complaint.getLocation().getY())));
        cluster.setReportCount(0);
        return clusterRepository.save(cluster);
    }

    private void recomputeRollups(ComplaintCluster cluster) {
        List<Complaint> members = complaintRepository.findByClusterId(cluster.getId());

        int maxRank = 0;
        for (Complaint member : members) {
            Optional<AiAnalysis> analysis = aiAnalysisRepository.findByComplaintId(member.getId());
            if (analysis.isPresent()) {
                Integer rank = SEVERITY_RANK.get(analysis.get().getSeverity());
                if (rank != null && rank > maxRank) {
                    maxRank = rank;
                }
            }
        }

        if (maxRank > 0) {
            String severity = RANK_SEVERITY.get(maxRank);
            cluster.setSeverity(severity);

            int priority = SEVERITY_PRIORITY.get(severity);
            if (cluster.getReportCount() >= denseReportThreshold && priority > 1) {
                priority--;
            }
            cluster.setPriority(priority);
        }

        clusterRepository.save(cluster);
    }
}
