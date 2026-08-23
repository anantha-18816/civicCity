package com.civicAI.backend.controller;

import com.civicAI.backend.dto.AssignRequest;
import com.civicAI.backend.dto.ComplaintRequest;
import com.civicAI.backend.dto.ComplaintResponse;
import com.civicAI.backend.dto.ComplaintStatusRequest;
import com.civicAI.backend.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintResponse createComplaint(@Valid @RequestBody ComplaintRequest request) {
        return complaintService.createComplaint(request);
    }

    @GetMapping("/{id}")
    public ComplaintResponse getComplaint(@PathVariable Long id) {
        return complaintService.getComplaint(id);
    }

    @GetMapping
    public List<ComplaintResponse> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    @PatchMapping("/{id}/status")
    public ComplaintResponse updateComplaintStatus(@PathVariable Long id,
                                                   @Valid @RequestBody ComplaintStatusRequest request) {
        return complaintService.updateComplaintStatus(id, request);
    }

    @PostMapping("/{id}/assign")
    public ComplaintResponse assignComplaint(@PathVariable Long id,
                                             @Valid @RequestBody AssignRequest request) {
        return complaintService.assignComplaint(id, request.getDepartmentId());
    }
}