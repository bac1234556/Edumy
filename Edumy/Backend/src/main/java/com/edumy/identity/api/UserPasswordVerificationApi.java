package com.edumy.identity.api;

public interface UserPasswordVerificationApi {
    boolean verifyPassword(Integer userId, String rawPassword);
}
