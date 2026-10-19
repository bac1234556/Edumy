package com.edumy.backend.repository;

import com.edumy.backend.model.CartItem;
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
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId AND (ci.courseId = :id OR ci.id = :id)")
    int deleteByCartIdAndCourseOrItemId(@Param("cartId") Integer cartId, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId AND ci.id = :itemId")
    int deleteByCartIdAndItemId(@Param("cartId") Integer cartId, @Param("itemId") Integer itemId);
}
