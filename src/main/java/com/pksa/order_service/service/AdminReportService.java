package com.pksa.order_service.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.pksa.order_service.dto.AdminReportRequest;
import com.pksa.order_service.dto.OrderDto;
import com.pksa.order_service.entity.Order;
import com.pksa.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {
    private final OrderRepository orderRepository;
    
    public byte[] generateReport(AdminReportRequest request) throws IOException {
        // Step 1: Calculate date range
        LocalDateTime startDate = calculateStartDate(request.getDuration());
        LocalDateTime endDate = LocalDateTime.now();
        
        // Step 2: Get orders from database
        List<Order> allOrders = orderRepository.findByOrderDateBetween(startDate, endDate);
        
        // Step 3: Apply filters using streams
        List<OrderDto> filteredOrders = allOrders.stream()
                .filter(order -> request.getStatus() == null || 
                        order.getStatus().equals(request.getStatus()))
                .filter(order -> request.getCustomerId() == null || 
                        order.getCustomerId().equals(request.getCustomerId()))
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .map(this::toDto)
                .toList();
        
        // Step 4: Generate report based on format
        return switch (request.getFormat().toUpperCase()) {
            case "EXCEL" -> generateExcelReport(filteredOrders, startDate, endDate);
            case "PDF" -> generatePdfReport(filteredOrders, startDate, endDate);
            case "WORD" -> generateWordReport(filteredOrders, startDate, endDate);
            default -> throw new IllegalArgumentException("Unsupported format: " + request.getFormat());
        };
    }
    
    private LocalDateTime calculateStartDate(String duration) {
        LocalDateTime now = LocalDateTime.now();
        return switch (duration) {
            case "1_WEEK" -> now.minusWeeks(1);
            case "1_MONTH" -> now.minusMonths(1);
            case "6_MONTHS" -> now.minusMonths(6);
            default -> now.minusMonths(1);
        };
    }
    
    private byte[] generateExcelReport(List<OrderDto> orders, LocalDateTime start, LocalDateTime end) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            XSSFSheet sheet = workbook.createSheet("Orders Report");
            
            // Title and period
            XSSFRow titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("ADMIN ORDERS REPORT");
            
            XSSFRow periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Period: " + start.format(DateTimeFormatter.ISO_LOCAL_DATE) + 
                    " to " + end.format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            XSSFRow summaryRow = sheet.createRow(2);
            summaryRow.createCell(0).setCellValue("Total Orders: " + orders.size());
            
            // Empty row
            sheet.createRow(3);
            
            // Header row
            XSSFRow headerRow = sheet.createRow(4);
            headerRow.createCell(0).setCellValue("Order ID");
            headerRow.createCell(1).setCellValue("Product ID");
            headerRow.createCell(2).setCellValue("Customer ID");
            headerRow.createCell(3).setCellValue("Quantity");
            headerRow.createCell(4).setCellValue("Order Date");
            headerRow.createCell(5).setCellValue("Status");
            
            // Data rows
            int rowNum = 5;
            for (OrderDto order : orders) {
                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getProductId());
                row.createCell(2).setCellValue(order.getCustomerId());
                row.createCell(3).setCellValue(order.getQuantity());
                row.createCell(4).setCellValue(order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                row.createCell(5).setCellValue(order.getStatus());
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    private byte[] generatePdfReport(List<OrderDto> orders, LocalDateTime start, LocalDateTime end) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Title
            document.add(new Paragraph("ADMIN ORDERS REPORT")
                    .setFontSize(18)
                    .setBold());
            
            document.add(new Paragraph("Period: " + start.format(DateTimeFormatter.ISO_LOCAL_DATE) + 
                    " to " + end.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            
            document.add(new Paragraph("Total Orders: " + orders.size()));
            document.add(new Paragraph("\n"));
            
            // Create table
            Table table = new Table(6);
            table.addHeaderCell("Order ID");
            table.addHeaderCell("Product ID");
            table.addHeaderCell("Customer ID");
            table.addHeaderCell("Quantity");
            table.addHeaderCell("Order Date");
            table.addHeaderCell("Status");
            
            for (OrderDto order : orders) {
                table.addCell(String.valueOf(order.getId()));
                table.addCell(String.valueOf(order.getProductId()));
                table.addCell(String.valueOf(order.getCustomerId()));
                table.addCell(String.valueOf(order.getQuantity()));
                table.addCell(order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                table.addCell(order.getStatus());
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
        }
    }
    
    private byte[] generateWordReport(List<OrderDto> orders, LocalDateTime start, LocalDateTime end) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Title
            XWPFParagraph title = document.createParagraph();
            XWPFRun titleRun = title.createRun();
            titleRun.setText("ADMIN ORDERS REPORT");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            
            // Period
            XWPFParagraph period = document.createParagraph();
            XWPFRun periodRun = period.createRun();
            periodRun.setText("Period: " + start.format(DateTimeFormatter.ISO_LOCAL_DATE) + 
                    " to " + end.format(DateTimeFormatter.ISO_LOCAL_DATE));
            
            // Summary
            XWPFParagraph summary = document.createParagraph();
            XWPFRun summaryRun = summary.createRun();
            summaryRun.setText("Total Orders: " + orders.size());
            
            // Table
            XWPFTable table = document.createTable();
            table.getRow(0).getCell(0).setText("Order ID");
            table.getRow(0).addNewTableCell().setText("Product ID");
            table.getRow(0).addNewTableCell().setText("Customer ID");
            table.getRow(0).addNewTableCell().setText("Quantity");
            table.getRow(0).addNewTableCell().setText("Order Date");
            table.getRow(0).addNewTableCell().setText("Status");
            
            for (OrderDto order : orders) {
                table.createRow();
                int rowIndex = table.getNumberOfRows() - 1;
                table.getRow(rowIndex).getCell(0).setText(String.valueOf(order.getId()));
                table.getRow(rowIndex).getCell(1).setText(String.valueOf(order.getProductId()));
                table.getRow(rowIndex).getCell(2).setText(String.valueOf(order.getCustomerId()));
                table.getRow(rowIndex).getCell(3).setText(String.valueOf(order.getQuantity()));
                table.getRow(rowIndex).getCell(4).setText(order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                table.getRow(rowIndex).getCell(5).setText(order.getStatus());
            }
            
            document.write(out);
            return out.toByteArray();
        }
    }
    
    private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .customerId(order.getCustomerId())
                .quantity(order.getQuantity())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .build();
    }
}
