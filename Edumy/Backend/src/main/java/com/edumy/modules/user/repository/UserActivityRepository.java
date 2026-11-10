package com.edumy.modules.user.repository;

import com.edumy.modules.user.model.UserActivity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Integer> {
}
