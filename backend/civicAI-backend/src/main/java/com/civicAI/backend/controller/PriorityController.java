package com.civicAI.backend.controller;

import com.civicAI.backend.dto.PriorityItem;
import com.civicAI.backend.service.PriorityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/complaints/priority")
public class PriorityController {

    private final PriorityService priorityService;

    public PriorityController(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    @GetMapping("/queue")
    public List<PriorityItem> queue() {
        return priorityService.queue();
    }
}
