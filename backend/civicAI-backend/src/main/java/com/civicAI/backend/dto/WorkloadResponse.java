package com.civicAI.backend.dto;

import java.util.Map;

public class WorkloadResponse {

    private Long departmentId;
    private String code;
    private String name;
    private long totalOpen;
    private Map<Integer, Long> byPriority;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalOpen() {
        return totalOpen;
    }

    public void setTotalOpen(long totalOpen) {
        this.totalOpen = totalOpen;
    }

    public Map<Integer, Long> getByPriority() {
        return byPriority;
    }

    public void setByPriority(Map<Integer, Long> byPriority) {
        this.byPriority = byPriority;
    }
}
