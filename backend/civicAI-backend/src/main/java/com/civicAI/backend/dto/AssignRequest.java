package com.civicAI.backend.dto;

import jakarta.validation.constraints.NotNull;

public class AssignRequest {

    @NotNull(message = "departmentId is required")
    private Long departmentId;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
