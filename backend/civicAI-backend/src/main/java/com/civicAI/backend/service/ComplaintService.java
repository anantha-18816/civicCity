package com.civicAI.backend.service;

import com.civicAI.backend.dto.ComplaintRequest;
import com.civicAI.backend.dto.ComplaintResponse;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.repository.ComplaintRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
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
        response.setCreatedAt(complaint.getCreatedAt());

        return response;
    }
}