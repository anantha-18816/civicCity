package com.civicAI.backend.controller;

import com.civicAI.backend.dto.ClusterResponse;
import com.civicAI.backend.entity.ComplaintCluster;
import com.civicAI.backend.repository.ComplaintClusterRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clusters")
public class ClusterController {

    private final ComplaintClusterRepository clusterRepository;

    public ClusterController(ComplaintClusterRepository clusterRepository) {
        this.clusterRepository = clusterRepository;
    }

    @GetMapping
    public List<ClusterResponse> getClusters() {
        return clusterRepository.findAll().stream()
                .map(ClusterController::toResponse)
                .toList();
    }

    static ClusterResponse toResponse(ComplaintCluster cluster) {
        ClusterResponse response = new ClusterResponse();
        response.setId(cluster.getId());
        response.setIssueType(cluster.getIssueType());
        response.setLatitude(cluster.getLocation().getY());
        response.setLongitude(cluster.getLocation().getX());
        response.setReportCount(cluster.getReportCount());
        response.setSeverity(cluster.getSeverity());
        response.setPriority(cluster.getPriority());
        response.setCreatedAt(cluster.getCreatedAt());
        return response;
    }
}
