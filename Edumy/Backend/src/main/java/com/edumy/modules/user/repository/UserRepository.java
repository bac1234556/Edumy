package com.edumy.modules.user.repository;

import com.edumy.modules.user.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
