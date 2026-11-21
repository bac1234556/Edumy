package com.edumy.identity.service;

import com.edumy.identity.user.repository.UserRepository;
import com.edumy.shared.security.UserActiveChecker;
import org.springframework.stereotype.Service;

@Service
public class UserActiveCheckerImpl implements UserActiveChecker {

    private final UserRepository userRepository;

    public UserActiveCheckerImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isUserActive(Integer userId) {
        if (userId == null) {
            return false;
        }
        return userRepository.findById(userId)
                .map(u -> !Boolean.FALSE.equals(u.getIsActive()))
                .orElse(false);
    }
}
