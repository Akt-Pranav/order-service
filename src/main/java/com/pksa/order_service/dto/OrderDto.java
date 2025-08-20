package com.pksa.order_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Integer id;
    private Integer productId;
    private Integer customerId;
    private Integer quantity;
    private LocalDateTime orderDate;
    private String status; // e.g., NEW, SHIPPED, DELIVERED
}
