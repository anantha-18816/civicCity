package com.civicAI.backend.repository;

import com.civicAI.backend.entity.AiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {

    Optional<AiAnalysis> findByComplaintId(Long complaintId);
}