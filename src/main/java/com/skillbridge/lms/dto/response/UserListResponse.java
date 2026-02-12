package com.skillbridge.lms.dto.response;

import java.time.LocalDateTime;

import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserListResponse {

    private Long id;
    private String email;
    private String name;
    private UserRole role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public static UserListResponse from(User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
