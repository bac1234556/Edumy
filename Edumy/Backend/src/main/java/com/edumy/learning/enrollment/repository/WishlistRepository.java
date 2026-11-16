package com.edumy.learning.enrollment.repository;

import com.edumy.learning.enrollment.model.Wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
}
