package com.pksa.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderDto {
    @NotNull
    private Integer productId;
    @NotNull
    private Integer customerId;
    @Min(1)
    private Integer quantity;
}
