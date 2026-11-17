package com.edumy.commerce.order.repository;

import com.edumy.commerce.order.model.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);

    @Query("""
            SELECT COALESCE(SUM(oi.price), 0) FROM OrderItem oi
            WHERE oi.courseId IN :courseIds
              AND oi.order IS NOT NULL
              AND (UPPER(oi.order.status) = 'PAID' OR UPPER(oi.order.status) = 'COMPLETED')
            """)
    BigDecimal sumPaidGrossSalesByCourseIds(@Param("courseIds") List<Integer> courseIds);
}
