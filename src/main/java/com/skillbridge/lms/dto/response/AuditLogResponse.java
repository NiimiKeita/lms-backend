package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.AuditLog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog auditLog) {
        return from(auditLog, null);
    }

    public static AuditLogResponse from(AuditLog auditLog, String userName) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .userName(userName)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
