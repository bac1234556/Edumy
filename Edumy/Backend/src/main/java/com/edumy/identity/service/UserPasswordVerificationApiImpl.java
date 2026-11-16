package com.edumy.identity.service;

import com.edumy.identity.api.UserPasswordVerificationApi;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserPasswordVerificationApiImpl implements UserPasswordVerificationApi {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserPasswordVerificationApiImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean verifyPassword(Integer userId, String rawPassword) {
        if (userId == null || rawPassword == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getPasswordHash() == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
