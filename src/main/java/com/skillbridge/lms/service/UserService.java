package com.skillbridge.lms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillbridge.lms.dto.request.UpdateProfileRequest;
import com.skillbridge.lms.dto.response.ProfileResponse;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * プロフィール取得
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        return ProfileResponse.from(user);
    }

    /**
     * プロフィール更新
     */
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);
        user.setUsername(request.getUsername());
        user = userRepository.save(user);
        return ProfileResponse.from(user);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
    }
}
