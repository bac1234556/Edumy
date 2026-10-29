package com.edumy.services;

import com.edumy.annotations.Loggable;
import com.edumy.annotations.Monitored;
import com.edumy.dto.UpdateProfileRequest;
import com.edumy.dto.UserProfileResponse;
import com.edumy.entities.User;
import com.edumy.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Loggable
    @Monitored("user.get-profile")
    public UserProfileResponse getProfile(int userId) {
        User user = findByIdOrThrow(userId);
        return toProfileResponse(user);
    }

    @Transactional
    @Loggable
    @Monitored("user.update-profile")
    public UserProfileResponse updateProfile(int userId, UpdateProfileRequest request) {
        User user = findByIdOrThrow(userId);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        return toProfileResponse(user);
    }

    private User findByIdOrThrow(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt());
    }
}
