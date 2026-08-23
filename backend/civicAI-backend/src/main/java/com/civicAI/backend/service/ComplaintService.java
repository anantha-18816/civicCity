package com.civicAI.backend.service;

import com.civicAI.backend.dto.ComplaintRequest;
import com.civicAI.backend.dto.ComplaintResponse;
import com.civicAI.backend.dto.ComplaintStatusRequest;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.exception.ResourceNotFoundException;
import com.civicAI.backend.exception.InvalidStateException;
import com.civicAI.backend.repository.ComplaintRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final RoutingService routingService;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    public ComplaintService(ComplaintRepository complaintRepository,
                            RoutingService routingService) {
        this.complaintRepository = complaintRepository;
        this.routingService = routingService;
    }

    public ComplaintResponse assignComplaint(Long id, Long departmentId) {
        return toResponse(routingService.assignManually(id, departmentId));
    }

    public ComplaintResponse createComplaint(ComplaintRequest request) {

        Complaint complaint = new Complaint();

        complaint.setUserId(request.getUserId());
        complaint.setIssueType(request.getIssueType());
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());

        Point location = geometryFactory.createPoint(
                new Coordinate(
                        request.getLongitude(),
                        request.getLatitude()
                )
        );

        complaint.setLocation(location);

        return toResponse(complaintRepository.save(complaint));
    }

    public ComplaintResponse getComplaint(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        return toResponse(complaint);
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ComplaintResponse updateComplaintStatus(Long id, ComplaintStatusRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        String newStatus = request.getStatus().toUpperCase();

        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status '" + request.getStatus() + "'. Allowed values: " + VALID_STATUSES);
        }

        if (!ALLOWED_TRANSITIONS.getOrDefault(complaint.getStatus(), Set.of()).contains(newStatus)) {
            throw new InvalidStateException("Illegal status transition from '"
                    + complaint.getStatus() + "' to '" + newStatus + "'");
        }

        complaint.setStatus(newStatus);

        if ("RESOLVED".equals(newStatus)) {
            complaint.setResolvedAt(OffsetDateTime.now());
        }

        return toResponse(complaintRepository.save(complaint));
    }

    private static final Set<String> VALID_STATUSES = Set.of(
            "SUBMITTED", "AI_ANALYZED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED"
    );

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "SUBMITTED", Set.of("AI_ANALYZED", "REJECTED"),
            "AI_ANALYZED", Set.of("ASSIGNED", "IN_PROGRESS", "REJECTED"),
            "ASSIGNED", Set.of("IN_PROGRESS", "RESOLVED", "REJECTED"),
            "IN_PROGRESS", Set.of("RESOLVED", "REJECTED")
    );

    private ComplaintResponse toResponse(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();

        response.setId(complaint.getId());
        response.setUserId(complaint.getUserId());
        response.setIssueType(complaint.getIssueType());
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setLatitude(complaint.getLocation().getY());
        response.setLongitude(complaint.getLocation().getX());
        response.setStatus(complaint.getStatus());
        response.setPriority(complaint.getPriority());
        response.setDuplicateOfId(complaint.getDuplicateOfId());
        response.setDuplicateScore(complaint.getDuplicateScore());
        response.setClusterId(complaint.getClusterId());
        response.setDepartmentId(complaint.getDepartmentId());
        response.setCreatedAt(complaint.getCreatedAt());

        return response;
    }
}
