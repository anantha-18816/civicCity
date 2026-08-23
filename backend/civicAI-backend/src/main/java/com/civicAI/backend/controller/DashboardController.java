package com.civicAI.backend.controller;

import com.civicAI.backend.dto.AiInsights;
import com.civicAI.backend.dto.HeatPoint;
import com.civicAI.backend.dto.MapPoint;
import com.civicAI.backend.dto.ResolutionStats;
import com.civicAI.backend.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/map")
    public List<MapPoint> map() {
        return dashboardService.complaintMap();
    }

    @GetMapping("/heatmap")
    public List<HeatPoint> heatmap() {
        return dashboardService.heatmap();
    }

    @GetMapping("/resolution-stats")
    public ResolutionStats resolutionStats() {
        return dashboardService.resolutionStats();
    }

    @GetMapping("/ai-insights")
    public AiInsights aiInsights() {
        return dashboardService.aiInsights();
    }
}
