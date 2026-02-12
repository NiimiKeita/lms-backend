package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.skillbridge.lms.dto.request.AdminCreateUserRequest;
import com.skillbridge.lms.dto.request.AdminUpdateUserRequest;
import com.skillbridge.lms.dto.response.PageResponse;
import com.skillbridge.lms.dto.response.UserListResponse;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.enums.UserRole;
import com.skillbridge.lms.exception.BadRequestException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    private User adminUser;
    private User learnerUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .password("encoded")
                .username("Admin User")
                .role(UserRole.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        learnerUser = User.builder()
                .id(2L)
                .email("learner@example.com")
                .password("encoded")
                .username("Learner User")
                .role(UserRole.LEARNER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ===== getUsers =====

    @Test
    @DisplayName("getUsers - キーワードもロールも指定なしで全件取得")
    void getUsers_noFilter_returnsAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(adminUser, learnerUser), pageable, 2);
        when(userRepository.findAll(pageable)).thenReturn(page);

        PageResponse<UserListResponse> result = adminUserService.getUsers(null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getUsers - キーワード検索")
    void getUsers_withKeyword_searchesByKeyword() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(learnerUser), pageable, 1);
        when(userRepository.searchByKeyword(eq("learner"), eq(pageable))).thenReturn(page);

        PageResponse<UserListResponse> result = adminUserService.getUsers("learner", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("learner@example.com");
    }

    @Test
    @DisplayName("getUsers - ロールフィルタ")
    void getUsers_withRole_filtersByRole() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(adminUser), pageable, 1);
        when(userRepository.findByRole(eq(UserRole.ADMIN), eq(pageable))).thenReturn(page);

        PageResponse<UserListResponse> result = adminUserService.getUsers(null, "ADMIN", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("getUsers - キーワード+ロールの組み合わせ検索")
    void getUsers_withKeywordAndRole_searchesBoth() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> page = new PageImpl<>(List.of(learnerUser), pageable, 1);
        when(userRepository.searchByRoleAndKeyword(eq(UserRole.LEARNER), eq("learner"), eq(pageable)))
                .thenReturn(page);

        PageResponse<UserListResponse> result = adminUserService.getUsers("learner", "LEARNER", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getUsers - 無効なロールでBadRequestException")
    void getUsers_invalidRole_throwsBadRequest() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThatThrownBy(() -> adminUserService.getUsers(null, "INVALID", pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("無効なロール");
    }

    // ===== getUser =====

    @Test
    @DisplayName("getUser - 存在するユーザーの取得")
    void getUser_existingUser_returnsResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        UserListResponse result = adminUserService.getUser(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("admin@example.com");
        assertThat(result.getName()).isEqualTo("Admin User");
    }

    @Test
    @DisplayName("getUser - 存在しないユーザーでResourceNotFoundException")
    void getUser_nonExisting_throwsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ユーザーが見つかりません");
    }

    // ===== createUser =====

    @Test
    @DisplayName("createUser - 正常作成")
    void createUser_validRequest_createsUser() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setName("New User");
        request.setRole("LEARNER");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserListResponse result = adminUserService.createUser(request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getRole()).isEqualTo(UserRole.LEARNER);
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("createUser - メール重複でBadRequestException")
    void createUser_duplicateEmail_throwsBadRequest() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setName("Duplicate");
        request.setRole("LEARNER");

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("既に使用されています");
        verify(userRepository, never()).save(any());
    }

    // ===== updateUser =====

    @Test
    @DisplayName("updateUser - 正常更新")
    void updateUser_validRequest_updatesUser() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setName("Updated Name");
        request.setRole("INSTRUCTOR");

        when(userRepository.findById(2L)).thenReturn(Optional.of(learnerUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserListResponse result = adminUserService.updateUser(2L, request);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getRole()).isEqualTo(UserRole.INSTRUCTOR);
    }

    @Test
    @DisplayName("updateUser - 存在しないユーザーでResourceNotFoundException")
    void updateUser_nonExisting_throwsNotFound() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setName("Name");
        request.setRole("LEARNER");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateUser(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ===== toggleEnabled =====

    @Test
    @DisplayName("toggleEnabled - 有効→無効に切替")
    void toggleEnabled_enabledUser_disables() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(learnerUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserListResponse result = adminUserService.toggleEnabled(2L, 1L);

        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("toggleEnabled - 無効→有効に切替")
    void toggleEnabled_disabledUser_enables() {
        learnerUser.setEnabled(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(learnerUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserListResponse result = adminUserService.toggleEnabled(2L, 1L);

        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("toggleEnabled - 自分自身の無効化でBadRequestException")
    void toggleEnabled_selfDisable_throwsBadRequest() {
        assertThatThrownBy(() -> adminUserService.toggleEnabled(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("自分自身");
        verify(userRepository, never()).findById(any());
    }
}
