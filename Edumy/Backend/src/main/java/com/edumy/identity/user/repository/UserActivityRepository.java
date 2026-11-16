package com.edumy.identity.user.repository;

import com.edumy.identity.user.model.UserActivity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Integer> {
}
