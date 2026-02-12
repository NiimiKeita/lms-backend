package com.skillbridge.lms.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.AuditLogResponse;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.entity.AuditLog;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.repository.AuditLogRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(Long userId, String action, String entityType, Long entityId, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(String action, String entityType, Long userId,
            Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findWithFilters(action, entityType, userId, pageable);

        // Build user name map
        var userIds = page.getContent().stream()
                .map(AuditLog::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        var content = page.getContent().stream()
                .map(log -> AuditLogResponse.from(log, userNameMap.get(log.getUserId())))
                .toList();

        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("ID,User ID,Action,Entity Type,Entity ID,Details,IP Address,Created At");

        var logs = auditLogRepository.findAll();
        for (AuditLog log : logs) {
            pw.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    log.getUserId() != null ? log.getUserId() : "",
                    escapeCsv(log.getAction()),
                    escapeCsv(log.getEntityType()),
                    log.getEntityId() != null ? log.getEntityId() : "",
                    escapeCsv(log.getDetails()),
                    escapeCsv(log.getIpAddress()),
                    log.getCreatedAt());
        }

        pw.flush();
        return sw.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
