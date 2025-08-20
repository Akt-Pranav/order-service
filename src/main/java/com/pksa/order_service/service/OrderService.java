package com.pksa.order_service.service;

import com.pksa.order_service.dto.*;
import com.pksa.order_service.entity.Order;
import com.pksa.order_service.repository.OrderRepository;
import com.pksa.order_service.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final WebClient webClient = WebClient.create("http://localhost:8081");

    public List<OrderDto> getAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public OrderDto getById(int id) {
        Order order = repository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return toDto(order);
    }

    public OrderDto placeOrder(CreateOrderDto dto) {
        try {
            ProductDto product = webClient.get()
                .uri("/products/" + dto.getProductId())  
                .retrieve()
                .bodyToMono(ProductDto.class)  
                .block();

            if (product == null) {
                throw new RuntimeException("Product with ID " + dto.getProductId() + " not found!");
            }

            Order order = Order.builder()
                    .productId(dto.getProductId())
                    .customerId(dto.getCustomerId())
                    .quantity(dto.getQuantity())
                    .orderDate(LocalDateTime.now())
                    .status("NEW")
                    .build();
            
            order = repository.save(order);
            logger.info("Order placed: {} for Product: {}", order.getId(), product.getName());
            return toDto(order);
            
        } catch (Exception e) {
            logger.error("Failed to place order for product {}: {}", dto.getProductId(), e.getMessage());
            throw new RuntimeException("Failed to place order: " + e.getMessage());
        }
    }

    public List<OrderDto> getOrdersByCustomer(int customerId) {
        return repository.findByCustomerId(customerId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public void delete(int id) {
        repository.deleteById(id);
        logger.warn("Order deleted: {}", id);
    }

    public OrderDto updateStatus(int id, String status) {
        Order order = repository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        order.setStatus(status);
        return toDto(repository.save(order));
    }

    private OrderDto toDto(Order o) {
        return OrderDto.builder()
                .id(o.getId())
                .productId(o.getProductId())
                .customerId(o.getCustomerId())
                .quantity(o.getQuantity())
                .orderDate(o.getOrderDate())
                .status(o.getStatus())
                .build();
    }
}
