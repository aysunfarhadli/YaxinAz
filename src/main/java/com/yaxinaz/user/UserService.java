package com.yaxinaz.user;

import com.yaxinaz.exception.UserNotFoundException;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.dto.ChangePasswordRequest;
import com.yaxinaz.user.dto.UpdateProfileRequest;
import com.yaxinaz.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setAvatarUrl(request.avatarUrl());
        user.setPreferredLanguage(request.preferredLanguage());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AccessDeniedException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    private User currentUser() {
        Long userId = SecurityUtils.currentUserId();
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));
    }
}
