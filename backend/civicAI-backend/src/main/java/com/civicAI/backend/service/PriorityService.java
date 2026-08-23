package com.civicAI.backend.service;

import com.civicAI.backend.dto.PriorityItem;
import com.civicAI.backend.entity.AiAnalysis;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.repository.AiAnalysisRepository;
import com.civicAI.backend.repository.ComplaintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PriorityService {

    private static final Logger log = LoggerFactory.getLogger(PriorityService.class);

    private static final Set<String> OPEN_STATUSES =
            Set.of("SUBMITTED", "AI_ANALYZED", "ASSIGNED", "IN_PROGRESS");

    private static final Map<String, Integer> SEVERITY_BASE = Map.of(
            "HIGH", 70, "MEDIUM", 45, "LOW", 20);

    private static final Map<String, Integer> RISK_BONUS = Map.of(
            "HIGH", 15, "MEDIUM", 7, "LOW", 0);

    private final ComplaintRepository complaintRepository;
    private final AiAnalysisRepository aiAnalysisRepository;

    private final int maxReportBonus;
    private final double reportBonusStep;
    private final int graceDays;
    private final double dailyAgeBonus;
    private final int maxAgeBonus;
    private final Double centerLatitude;
    private final Double centerLongitude;
    private final double centerRadiusM;
    private final int locationBonus;
    private final Set<String> roadIssueTypes;
    private final int roadImportanceBonus;

    public PriorityService(ComplaintRepository complaintRepository,
                           AiAnalysisRepository aiAnalysisRepository,
                           @Value("${priority.max-report-bonus:20}") int maxReportBonus,
                           @Value("${priority.report-bonus-step:4}") double reportBonusStep,
                           @Value("${priority.grace-days:3}") int graceDays,
                           @Value("${priority.daily-age-bonus:1.5}") double dailyAgeBonus,
                           @Value("${priority.max-age-bonus:15}") int maxAgeBonus,
                           @Value("${priority.center-latitude:}") Double centerLatitude,
                           @Value("${priority.center-longitude:}") Double centerLongitude,
                           @Value("${priority.center-radius-m:5000}") double centerRadiusM,
                           @Value("${priority.location-bonus:10}") int locationBonus,
                           @Value("${priority.road-issue-types:POTHOLE,ROAD_DAMAGE}") String roadIssueTypes,
                           @Value("${priority.road-importance-bonus:8}") int roadImportanceBonus) {
        this.complaintRepository = complaintRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.maxReportBonus = maxReportBonus;
        this.reportBonusStep = reportBonusStep;
        this.graceDays = graceDays;
        this.dailyAgeBonus = dailyAgeBonus;
        this.maxAgeBonus = maxAgeBonus;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.centerRadiusM = centerRadiusM;
        this.locationBonus = locationBonus;
        this.roadIssueTypes = Arrays.stream(roadIssueTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        this.roadImportanceBonus = roadImportanceBonus;
    }

    /**
     * Computes a 0-100 priority score from the factors defined in the roadmap:
     * AI severity, number of reports, location, road importance,
     * time unresolved and safety risk.
     */
    public ScoredPriority compute(Complaint complaint) {
        Optional<AiAnalysis> analysisOpt = aiAnalysisRepository.findByComplaintId(complaint.getId());
        AiAnalysis analysis = analysisOpt.orElse(null);

        double score = 0;

        String severity = analysis != null ? analysis.getSeverity() : null;
        if (severity != null) {
            score += SEVERITY_BASE.getOrDefault(severity.toUpperCase(), 10);
        } else {
            score += 10;
        }

        String roadRisk = analysis != null ? analysis.getRoadRisk() : null;
        if (roadRisk != null) {
            score += RISK_BONUS.getOrDefault(roadRisk.toUpperCase(), 0);
        }

        int extraReports = Math.max(0, reportCountFor(complaint) - 1);
        score += Math.min(maxReportBonus, extraReports * reportBonusStep);

        long ageDays = ageInDays(complaint);
        if (ageDays > graceDays) {
            score += Math.min(maxAgeBonus, (ageDays - graceDays) * dailyAgeBonus);
        }

        if (isNearCenter(complaint)) {
            score += locationBonus;
        }

        if (roadIssueTypes.contains(complaint.getIssueType().toUpperCase())) {
            score += roadImportanceBonus;
        }

        score = Math.min(100.0, score);

        return new ScoredPriority(round2(score), toBucket(score));
    }

    public List<PriorityItem> queue() {
        return complaintRepository.findAll().stream()
                .filter(c -> OPEN_STATUSES.contains(c.getStatus()))
                .map(this::toItem)
                .sorted(Comparator.comparingDouble(PriorityItem::getScore).reversed())
                .toList();
    }

    public void refreshPriorities() {
        List<Complaint> open = complaintRepository.findAll().stream()
                .filter(c -> OPEN_STATUSES.contains(c.getStatus()))
                .toList();

        int changed = 0;
        for (Complaint complaint : open) {
            ScoredPriority scored = compute(complaint);
            if (!Integer.valueOf(scored.priority()).equals(complaint.getPriority())) {
                complaint.setPriority(scored.priority());
                complaintRepository.save(complaint);
                changed++;
            }
        }
        log.info("Priority refresh complete: {} complaints evaluated, {} updated",
                open.size(), changed);
    }

    private PriorityItem toItem(Complaint complaint) {
        ScoredPriority scored = compute(complaint);

        PriorityItem item = new PriorityItem();
        item.setComplaintId(complaint.getId());
        item.setTitle(complaint.getTitle());
        item.setIssueType(complaint.getIssueType());
        item.setStatus(complaint.getStatus());
        item.setPriority(toBucket(scored.score()));
        item.setScore(scored.score());

        Optional<AiAnalysis> analysis = aiAnalysisRepository.findByComplaintId(complaint.getId());
        item.setSeverity(analysis.map(AiAnalysis::getSeverity).orElse(null));
        item.setRoadRisk(analysis.map(AiAnalysis::getRoadRisk).orElse(null));

        item.setReportCount(reportCountFor(complaint));
        item.setAgeDays(ageInDays(complaint));
        item.setLatitude(complaint.getLocation().getY());
        item.setLongitude(complaint.getLocation().getX());

        return item;
    }

    private int reportCountFor(Complaint complaint) {
        if (complaint.getClusterId() == null) {
            return 1;
        }
        return complaintRepository.findByClusterId(complaint.getClusterId()).size();
    }

    private long ageInDays(Complaint complaint) {
        return Duration.between(complaint.getCreatedAt(), OffsetDateTime.now()).toDays();
    }

    private boolean isNearCenter(Complaint complaint) {
        if (centerLatitude == null || centerLongitude == null
                || centerLatitude.isNaN() || centerLongitude.isNaN()) {
            return false;
        }
        double dLat = Math.toRadians(complaint.getLocation().getY() - centerLatitude);
        double dLon = Math.toRadians(complaint.getLocation().getX() - centerLongitude);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(centerLatitude))
                * Math.cos(Math.toRadians(complaint.getLocation().getY()))
                * Math.pow(Math.sin(dLon / 2), 2);
        double meters = 2 * 6_371_000 * Math.asin(Math.sqrt(h));
        return meters <= centerRadiusM;
    }

    private int toBucket(double score) {
        if (score >= 80) {
            return 1;
        }
        if (score >= 55) {
            return 2;
        }
        if (score >= 30) {
            return 3;
        }
        return 4;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record ScoredPriority(double score, int priority) {
    }
}
