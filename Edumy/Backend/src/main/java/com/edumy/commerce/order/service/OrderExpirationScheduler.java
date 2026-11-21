package com.edumy.commerce.order.service;

import com.edumy.commerce.order.model.Order;
import com.edumy.commerce.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UC-S16 / NFR-09: Scheduled job that automatically marks overdue Pending orders as EXPIRED.
 */
@Component
public class OrderExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderExpirationScheduler.class);

    private final OrderRepository orderRepository;

    @Value("${edumy.orders.ttl-minutes:15}")
    private long orderTtlMinutes;

    public OrderExpirationScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedDelayString = "${edumy.orders.expire-check-ms:60000}")
    @Transactional
    public void expireOverduePendingOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(Math.max(1, orderTtlMinutes));
        List<Order> pending = orderRepository.findByStatus("Pending");
        int expiredCount = 0;
        for (Order order : pending) {
            if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(cutoff)) {
                order.setStatus("EXPIRED");
                orderRepository.save(order);
                expiredCount++;
            }
        }
        if (expiredCount > 0) {
            logger.info("OrderExpirationScheduler expired {} overdue pending order(s).", expiredCount);
        }
    }
}
