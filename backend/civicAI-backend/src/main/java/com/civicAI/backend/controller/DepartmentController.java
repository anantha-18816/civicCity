package com.civicAI.backend.controller;

import com.civicAI.backend.dto.WorkloadResponse;
import com.civicAI.backend.entity.Department;
import com.civicAI.backend.repository.DepartmentRepository;
import com.civicAI.backend.service.RoutingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final RoutingService routingService;

    public DepartmentController(DepartmentRepository departmentRepository,
                                RoutingService routingService) {
        this.departmentRepository = departmentRepository;
        this.routingService = routingService;
    }

    @GetMapping
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @GetMapping("/workload")
    public List<WorkloadResponse> getWorkload() {
        return routingService.workload();
    }
}
