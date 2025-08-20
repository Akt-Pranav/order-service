package com.pksa.order_service.repository;

import com.pksa.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomerId(Integer customerId);
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
