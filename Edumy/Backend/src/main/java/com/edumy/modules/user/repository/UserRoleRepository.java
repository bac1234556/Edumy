package com.edumy.modules.user.repository;

import com.edumy.modules.user.model.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
}
