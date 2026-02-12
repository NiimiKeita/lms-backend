package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private String link;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.getIsRead())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
