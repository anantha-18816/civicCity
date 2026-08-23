package com.civicAI.backend.service;

import com.civicAI.backend.dto.WorkloadResponse;
import com.civicAI.backend.entity.Complaint;
import com.civicAI.backend.entity.Department;
import com.civicAI.backend.exception.ResourceNotFoundException;
import com.civicAI.backend.repository.ComplaintRepository;
import com.civicAI.backend.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private static final Set<String> ASSIGNABLE_STATUSES =
            Set.of("SUBMITTED", "AI_ANALYZED");

    private static final Set<String> OPEN_STATUSES =
            Set.of("SUBMITTED", "AI_ANALYZED", "ASSIGNED", "IN_PROGRESS");

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;

    private final Map<String, String> issueTypeMappings;
    private final String defaultDepartmentCode;

    public RoutingService(ComplaintRepository complaintRepository,
                          DepartmentRepository departmentRepository,
                          @Value("${routing.mappings:POTHOLE:ROADS,ROAD_DAMAGE:ROADS,GARBAGE:SANITATION,STREETLIGHT:LIGHTING,WATER_LOGGING:DRAINAGE}")
                          String mappings,
                          @Value("${routing.default-department-code:ROADS}") String defaultDepartmentCode) {
        this.complaintRepository = complaintRepository;
        this.departmentRepository = departmentRepository;
        this.defaultDepartmentCode = defaultDepartmentCode;
        this.issueTypeMappings = Arrays.stream(mappings.split(","))
                .map(String::trim)
                .filter(entry -> entry.contains(":"))
                .collect(Collectors.toMap(
                        entry -> entry.split(":")[0].trim().toUpperCase(),
                        entry -> entry.split(":")[1].trim().toUpperCase(),
                        (a, b) -> a));
    }

    /**
     * Automatically routes an analyzed complaint to the department mapped to
     * its issue type. Linked duplicates are skipped: they follow the
     * lifecycle of their original complaint.
     */
    public void autoAssign(Complaint complaint) {
        if (complaint.getDuplicateOfId() != null) {
            log.info("Complaint {} is a duplicate; skipping auto-assignment",
                    complaint.getId());
            return;
        }

        Department department = resolveDepartment(complaint.getIssueType());

        complaint.setDepartmentId(department.getId());
        if (ASSIGNABLE_STATUSES.contains(complaint.getStatus())) {
            complaint.setStatus("ASSIGNED");
        }
        complaintRepository.save(complaint);

        log.info("Complaint {} auto-assigned to department {} ({})",
                complaint.getId(), department.getCode(), department.getName());
    }

    public Complaint assignManually(Long complaintId, Long departmentId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Complaint not found with id: " + complaintId));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + departmentId));

        if (!OPEN_STATUSES.contains(complaint.getStatus())) {
            throw new IllegalStateException(
                    "Complaint " + complaintId + " cannot be assigned in status "
                            + complaint.getStatus());
        }

        complaint.setDepartmentId(department.getId());
        complaint.setStatus("ASSIGNED");

        return complaintRepository.save(complaint);
    }

    public List<WorkloadResponse> workload() {
        return departmentRepository.findAll().stream()
                .map(this::toWorkload)
                .sorted(java.util.Comparator.comparingLong(WorkloadResponse::getTotalOpen).reversed())
                .toList();
    }

    private WorkloadResponse toWorkload(Department department) {
        List<Complaint> open = complaintRepository.findAll().stream()
                .filter(c -> department.getId().equals(c.getDepartmentId()))
                .filter(c -> OPEN_STATUSES.contains(c.getStatus()))
                .toList();

        WorkloadResponse response = new WorkloadResponse();
        response.setDepartmentId(department.getId());
        response.setCode(department.getCode());
        response.setName(department.getName());
        response.setTotalOpen(open.size());
        response.setByPriority(open.stream()
                .map(Complaint::getPriority)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));
        return response;
    }

    private Department resolveDepartment(String issueType) {
        String code = issueTypeMappings.getOrDefault(
                issueType.toUpperCase(), defaultDepartmentCode);
        return departmentRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException(
                        "Configured department code '" + code + "' does not exist"));
    }
}
