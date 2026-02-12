package com.skillbridge.lms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.skillbridge.lms.dto.response.NotificationResponse;
import com.skillbridge.lms.entity.Notification;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.NotificationRepository;
import com.skillbridge.lms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").username("testuser").build();

        notification = Notification.builder()
                .id(1L)
                .user(user)
                .title("Test Notification")
                .message("Test Message")
                .type("INFO")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getNotifications - 通知一覧取得")
    void getNotifications_returnsNotifications() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.getNotifications("test@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Notification");
    }

    @Test
    @DisplayName("getUnreadCount - 未読数取得")
    void getUnreadCount_returnsCount() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);

        long count = notificationService.getUnreadCount("test@example.com");

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("markAsRead - 既読にマーク")
    void markAsRead_marksNotificationAsRead() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse result = notificationService.markAsRead(1L, "test@example.com");

        assertThat(result).isNotNull();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("markAsRead - 他ユーザーの通知は404")
    void markAsRead_otherUser_throwsException() {
        User otherUser = User.builder().id(2L).email("other@example.com").username("other").build();
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, "other@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("markAllAsRead - 全て既読にマーク")
    void markAllAsRead_marksAllAsRead() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        notificationService.markAllAsRead("test@example.com");

        verify(notificationRepository).saveAll(any());
    }

    @Test
    @DisplayName("createNotification - 通知作成")
    void createNotification_savesNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.createNotification(user, "Title", "Message", "INFO", "/link");

        verify(notificationRepository).save(any(Notification.class));
    }
}
