package com.civicAI.backend.service;

import com.civicAI.backend.dto.DuplicateMatch;
import com.civicAI.backend.entity.AiAnalysis;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.repository.AiAnalysisRepository;
import com.civicAI.backend.repository.ComplaintRepository;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DuplicateDetectionService {

    private static final Logger log = LoggerFactory.getLogger(DuplicateDetectionService.class);

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final int HASH_BITS = 64;

    private final ComplaintRepository complaintRepository;
    private final AiAnalysisRepository aiAnalysisRepository;

    private final double radiusM;
    private final double scoreThreshold;
    private final double spatialWeight;
    private final double imageWeight;
    private final int maxCandidates;

    public DuplicateDetectionService(ComplaintRepository complaintRepository,
                                     AiAnalysisRepository aiAnalysisRepository,
                                     @Value("${duplicate.radius-m:100}") double radiusM,
                                     @Value("${duplicate.score-threshold:0.7}") double scoreThreshold,
                                     @Value("${duplicate.spatial-weight:0.6}") double spatialWeight,
                                     @Value("${duplicate.image-weight:0.4}") double imageWeight,
                                     @Value("${duplicate.max-candidates:10}") int maxCandidates) {
        this.complaintRepository = complaintRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.radiusM = radiusM;
        this.scoreThreshold = scoreThreshold;
        this.spatialWeight = spatialWeight;
        this.imageWeight = imageWeight;
        this.maxCandidates = maxCandidates;
    }

    public DuplicateMatch detectDuplicate(Complaint analyzed, Long imageHash) {
        DuplicateMatch match = new DuplicateMatch();

        if (imageHash == null) {
            return match;
        }

        Point location = analyzed.getLocation();
        List<Complaint> candidates = complaintRepository.findDuplicateCandidates(
                analyzed.getIssueType(), analyzed.getId(),
                location.getY(), location.getX(), radiusM, maxCandidates);

        Complaint best = null;
        DuplicateMatch bestMatch = match;

        for (Complaint candidate : candidates) {
            Optional<AiAnalysis> candidateAnalysis =
                    aiAnalysisRepository.findByComplaintId(candidate.getId());
            if (candidateAnalysis.isEmpty() || candidateAnalysis.get().getImageHash() == null) {
                continue;
            }

            double distanceM = haversineM(location, candidate.getLocation());
            double imageSimilarity = hashSimilarity(imageHash, candidateAnalysis.get().getImageHash());

            DuplicateMatch current = new DuplicateMatch();
            current.setDistanceM(round2(distanceM));
            current.setImageSimilarity(round2(imageSimilarity));
            current.setScore(round2(spatialWeight * (1.0 - distanceM / radiusM)
                    + imageWeight * imageSimilarity));

            log.info("Duplicate candidate complaintId={} distance={}m imageSim={} score={}",
                    candidate.getId(), current.getDistanceM(),
                    current.getImageSimilarity(), current.getScore());

            if (best == null || current.getScore() > bestMatch.getScore()) {
                best = candidate;
                bestMatch = current;
            }
        }

        if (best != null && bestMatch.getScore() >= scoreThreshold) {
            bestMatch.setDuplicate(true);
            bestMatch.setDuplicateOfId(best.getId());

            analyzed.setDuplicateOfId(best.getId());
            analyzed.setDuplicateScore(bestMatch.getScore());
            complaintRepository.save(analyzed);

            log.info("Complaint {} marked as duplicate of {} (score={})",
                    analyzed.getId(), best.getId(), bestMatch.getScore());
        }

        return bestMatch;
    }

    private double haversineM(Point a, Point b) {
        double lat1 = Math.toRadians(a.getY());
        double lat2 = Math.toRadians(b.getY());
        double dLat = Math.toRadians(b.getY() - a.getY());
        double dLon = Math.toRadians(b.getX() - a.getX());

        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);
        double h = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;

        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(h));
    }

    private double hashSimilarity(long hashA, long hashB) {
        long xor = hashA ^ hashB;
        int differingBits = Long.bitCount(xor);
        return 1.0 - (double) differingBits / HASH_BITS;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
