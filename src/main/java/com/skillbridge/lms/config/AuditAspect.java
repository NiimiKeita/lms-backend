package com.skillbridge.lms.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @AfterReturning("execution(* com.skillbridge.lms.service.AdminUserService.create*(..)) || " +
            "execution(* com.skillbridge.lms.service.AdminUserService.update*(..)) || " +
            "execution(* com.skillbridge.lms.service.AdminUserService.toggle*(..)) || " +
            "execution(* com.skillbridge.lms.service.CourseService.create*(..)) || " +
            "execution(* com.skillbridge.lms.service.CourseService.update*(..)) || " +
            "execution(* com.skillbridge.lms.service.CourseService.delete*(..)) || " +
            "execution(* com.skillbridge.lms.service.CourseService.togglePublish(..)) || " +
            "execution(* com.skillbridge.lms.service.CategoryService.create*(..)) || " +
            "execution(* com.skillbridge.lms.service.CategoryService.update*(..)) || " +
            "execution(* com.skillbridge.lms.service.CategoryService.delete*(..))")
    public void auditAdminAction(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            String action = resolveAction(methodName);
            String entityType = resolveEntityType(className);

            Long userId = getCurrentUserId();
            String ipAddress = getClientIp();

            String details = className + "." + methodName;

            auditLogService.log(userId, action, entityType, null, details, ipAddress);
        } catch (Exception e) {
            // Audit logging should never break the main flow
        }
    }

    private String resolveAction(String methodName) {
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        if (methodName.startsWith("toggle")) return "UPDATE";
        return "OTHER";
    }

    private String resolveEntityType(String className) {
        if (className.contains("User")) return "USER";
        if (className.contains("Course")) return "COURSE";
        if (className.contains("Category")) return "CATEGORY";
        return "UNKNOWN";
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return userRepository.findByEmail(auth.getName())
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
