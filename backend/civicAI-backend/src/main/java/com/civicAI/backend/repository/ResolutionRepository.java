package com.civicAI.backend.repository;

import com.civicAI.backend.entity.Resolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResolutionRepository extends JpaRepository<Resolution, Long> {

    Optional<Resolution> findByComplaintId(Long complaintId);
}
