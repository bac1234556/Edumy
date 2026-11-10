package com.edumy.modules.enrollment.repository;

import com.edumy.modules.enrollment.model.Wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
}
