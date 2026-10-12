package com.edumy.backend.repository;

import com.edumy.backend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartId(Integer cartId);
    void deleteByCartIdAndCourseId(Integer cartId, Integer courseId);
    void deleteByCartId(Integer cartId);
}
