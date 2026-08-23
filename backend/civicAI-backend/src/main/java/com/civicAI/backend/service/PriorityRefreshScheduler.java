package com.civicAI.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriorityRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriorityRefreshScheduler.class);

    private final PriorityService priorityService;

    public PriorityRefreshScheduler(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    @Scheduled(cron = "${priority.refresh-cron:0 0 */6 * * *}")
    public void refresh() {
        log.info("Scheduled priority refresh starting");
        priorityService.refreshPriorities();
    }
}
