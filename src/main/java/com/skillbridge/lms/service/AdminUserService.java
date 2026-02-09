package com.skillbridge.lms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.skillbridge.lms.dto.request.AdminCreateUserRequest;
import com.skillbridge.lms.dto.request.AdminUpdateUserRequest;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.UserListResponse;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<UserListResponse> getUsers(String keyword, String role, Pageable pageable) {
        Page<User> page;
        UserRole roleFilter = parseRole(role);

        if (roleFilter != null && StringUtils.hasText(keyword)) {
            page = userRepository.searchByRoleAndKeyword(roleFilter, keyword, pageable);
        } else if (roleFilter != null) {
            page = userRepository.findByRole(roleFilter, pageable);
        } else if (StringUtils.hasText(keyword)) {
            page = userRepository.searchByKeyword(keyword, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        List<UserListResponse> content = page.getContent().stream()
                .map(UserListResponse::from)
                .toList();

        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public UserListResponse getUser(Long id) {
        User user = findUserById(id);
        return UserListResponse.from(user);
    }

    @Transactional
    public UserListResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("このメールアドレスは既に使用されています: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getName())
                .role(UserRole.valueOf(request.getRole()))
                .enabled(true)
                .build();

        user = userRepository.save(user);
        return UserListResponse.from(user);
    }

    @Transactional
    public UserListResponse updateUser(Long id, AdminUpdateUserRequest request) {
        User user = findUserById(id);
        user.setUsername(request.getName());
        user.setRole(UserRole.valueOf(request.getRole()));
        user = userRepository.save(user);
        return UserListResponse.from(user);
    }

    @Transactional
    public UserListResponse toggleEnabled(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BadRequestException("自分自身のアカウントを無効化することはできません");
        }

        User user = findUserById(id);
        user.setEnabled(!user.getEnabled());
        user = userRepository.save(user);
        return UserListResponse.from(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません: " + id));
    }

    private UserRole parseRole(String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("無効なロールです: " + role);
        }
    }
}
