package com.edumy.backend.repository;

import com.edumy.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    java.util.List<OrderItem> findByOrderId(Integer orderId);
}
