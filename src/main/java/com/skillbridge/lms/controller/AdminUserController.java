package com.skillbridge.lms.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillbridge.lms.dto.request.AdminCreateUserRequest;
import com.skillbridge.lms.dto.request.AdminUpdateUserRequest;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.UserListResponse;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<PageResponse<UserListResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<UserListResponse> response = adminUserService.getUsers(keyword, role, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserListResponse> getUser(@PathVariable Long id) {
        UserListResponse response = adminUserService.getUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserListResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        UserListResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserListResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        UserListResponse response = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle-enabled")
    public ResponseEntity<UserListResponse> toggleEnabled(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("現在のユーザーが見つかりません"));
        UserListResponse response = adminUserService.toggleEnabled(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
