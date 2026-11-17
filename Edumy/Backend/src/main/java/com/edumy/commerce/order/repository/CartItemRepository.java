package com.edumy.commerce.order.repository;

import com.edumy.commerce.order.model.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartId(Integer cartId);

    @Modifying
    @Transactional
    void deleteByCartIdAndCourseId(Integer cartId, Integer courseId);

    @Modifying
    @Transactional
    void deleteByCartId(Integer cartId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId AND ci.id = :itemId")
    int deleteByCartIdAndItemId(@Param("cartId") Integer cartId, @Param("itemId") Integer itemId);
}
