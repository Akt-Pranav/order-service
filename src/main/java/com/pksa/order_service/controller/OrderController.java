package com.pksa.order_service.controller;

import com.pksa.order_service.dto.*;
import com.pksa.order_service.service.OrderService;
import com.pksa.order_service.wrapper.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @GetMapping
    public ApiResponse<List<OrderDto>> getAll() {
        return ApiResponse.success(service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDto> getById(@PathVariable int id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<OrderDto> placeOrder(@RequestBody @Valid CreateOrderDto dto) {
        return ApiResponse.success(service.placeOrder(dto));
    }

    @GetMapping("/history")
    public ApiResponse<List<OrderDto>> getOrdersByCustomer(@RequestParam int customerId) {
        return ApiResponse.success(service.getOrdersByCustomer(customerId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable int id) {
        service.delete(id);
        return ApiResponse.success("Order deleted");
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderDto> updateStatus(@PathVariable int id, @RequestBody String status) {
        return ApiResponse.success(service.updateStatus(id, status));
    }
}
