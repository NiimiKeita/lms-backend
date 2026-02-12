package com.skillbridge.lms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.response.NotificationResponse;
import com.skillbridge.lms.entity.Notification;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.NotificationRepository;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotifications(String email) {
        User user = findUserByEmail(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long getUnreadCount(String email) {
        User user = findUserByEmail(email);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String email) {
        User user = findUserByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知が見つかりません: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("通知が見つかりません: " + notificationId);
        }

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = findUserByEmail(email);
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void createNotification(User user, String title, String message, String type, String link) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .link(link)
                .build();
        notificationRepository.save(notification);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
    }
}
