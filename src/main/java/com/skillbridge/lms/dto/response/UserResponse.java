package com.skillbridge.lms.dto.response;

import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private UserRole role;
    private Boolean enabled;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .build();
    }
}
