package com.civicAI.backend.repository;

import com.civicAI.backend.entity.ComplaintCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplaintClusterRepository extends JpaRepository<ComplaintCluster, Long> {

    @Query(value = """
            SELECT *
            FROM complaint_clusters cc
            WHERE cc.issue_type = :issueType
              AND ST_DWithin(
                    cc.location::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                    :radiusM)
            ORDER BY ST_Distance(
                    cc.location::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) ASC
            LIMIT 1
            """, nativeQuery = true)
    List<ComplaintCluster> findNearest(@Param("issueType") String issueType,
                                       @Param("latitude") double latitude,
                                       @Param("longitude") double longitude,
                                       @Param("radiusM") double radiusM);
}
