package com.pksa.order_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AdminReportRequest {
    @NotNull
    private String duration; // "1_WEEK", "1_MONTH", "6_MONTHS"
    
    @NotNull
    private String format; // "EXCEL", "PDF", "WORD"
    
    // Optional filters
    private String status; 
    private Integer customerId;
}
