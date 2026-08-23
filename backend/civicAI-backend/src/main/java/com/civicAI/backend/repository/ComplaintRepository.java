package com.civicAI.backend.repository;

import com.civicAI.backend.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    @Query(value = """
            SELECT *
            FROM complaints c
            WHERE c.id <> :excludeId
              AND c.issue_type = :issueType
              AND c.duplicate_of_id IS NULL
              AND c.status NOT IN ('RESOLVED', 'REJECTED')
              AND ST_DWithin(
                    c.location::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                    :radiusM)
            ORDER BY ST_Distance(
                    c.location::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Complaint> findDuplicateCandidates(@Param("issueType") String issueType,
                                            @Param("excludeId") Long excludeId,
                                            @Param("latitude") double latitude,
                                            @Param("longitude") double longitude,
                                            @Param("radiusM") double radiusM,
                                            @Param("limit") int limit);

    List<Complaint> findByClusterId(Long clusterId);

    long countByDuplicateOfIdNotNull();
}
