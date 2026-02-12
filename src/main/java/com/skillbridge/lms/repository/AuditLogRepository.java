package com.skillbridge.lms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skillbridge.lms.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:userId IS NULL OR a.userId = :userId) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> findWithFilters(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("userId") Long userId,
            Pageable pageable);
}
