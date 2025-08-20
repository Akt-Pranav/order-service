package com.pksa.order_service.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Data
public class EmailReportRequest {
@NotNull
private AdminReportRequest reportRequest;

@Email
@NotNull
private String emailTo;

private String subject = "Admin Report";
private String filename; 
}
