package com.edumy.commerce.order.repository;

import com.edumy.commerce.order.model.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    java.util.List<OrderItem> findByOrderId(Integer orderId);
}
