package com.pksa.order_service.controller;

import com.pksa.order_service.dto.AdminReportRequest;
import com.pksa.order_service.dto.EmailReportRequest;
import com.pksa.order_service.service.AdminReportService;
import com.pksa.order_service.service.PlainEmailService;
import com.pksa.order_service.wrapper.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class SimpleReportController {
    private final AdminReportService adminReportService;
    private final PlainEmailService emailService;

 // Download report (Excel/PDF/Word) with correct headers
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadReport(@RequestBody @Valid AdminReportRequest request) {
        try {
            byte[] reportData = adminReportService.generateReport(request);

            String filename = String.format("admin_report_%s.%s",
                    safeLower(request.getDuration()),
                    getFileExtension(request.getFormat()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(getMediaType(request.getFormat()))
                    .body(reportData);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .header("X-Error", e.getMessage())
                    .build();
        }
    }

    // Save report locally on server filesystem (./reports)
    @PostMapping("/save")
    public ApiResponse<String> saveLocally(@RequestBody @Valid AdminReportRequest request) {
        try {
            byte[] data = adminReportService.generateReport(request);
            String filename = String.format("admin_report_%s.%s",
                    safeLower(request.getDuration()),
                    getFileExtension(request.getFormat()));

            Path folder = Paths.get("reports");
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            Path filePath = folder.resolve(filename);
            Files.write(filePath, data);

            return ApiResponse.success("Saved at: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            return ApiResponse.fail("Failed to save report: " + e.getMessage());
        }
    }

    // Email the report as a plain text email with attachment (no template)
    @PostMapping("/email")
    public ApiResponse<String> emailReport(@RequestBody @Valid EmailReportRequest request) {
        try {
            byte[] reportData = adminReportService.generateReport(request.getReportRequest());

            String defaultName = String.format("admin_report_%s.%s",
                    safeLower(request.getReportRequest().getDuration()),
                    getFileExtension(request.getReportRequest().getFormat()));

            String filename = StringUtils.hasText(request.getFilename()) ? request.getFilename() : defaultName;

            emailService.sendWithAttachment(
                    request.getEmailTo(),
                    request.getSubject(),
                    "Please find the requested report attached.",
                    reportData,
                    filename
            );

            return ApiResponse.success("Report emailed to " + request.getEmailTo());
        } catch (Exception e) {
            return ApiResponse.fail("Failed to email report: " + e.getMessage());
        }
    }

    private String safeLower(String v) {
        return v == null ? "" : v.toLowerCase();
    }

    private String getFileExtension(String format) {
        String f = format == null ? "" : format.toUpperCase();
        switch (f) {
            case "EXCEL": return "xlsx";
            case "PDF": return "pdf";
            case "WORD": return "docx";
            default: return "dat";
        }
    }

    private MediaType getMediaType(String format) {
        String f = format == null ? "" : format.toUpperCase();
        switch (f) {
            case "EXCEL":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "PDF":
                return MediaType.APPLICATION_PDF;
            case "WORD":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}