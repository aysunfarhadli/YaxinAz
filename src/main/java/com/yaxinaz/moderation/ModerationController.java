package com.yaxinaz.moderation;

import com.yaxinaz.moderation.dto.ContentReportResponse;
import com.yaxinaz.moderation.dto.CreateContentReportRequest;
import com.yaxinaz.moderation.dto.UpdateReportStatusRequest;
import com.yaxinaz.response.PagedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moderation")
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/api/reports")
    public ResponseEntity<ContentReportResponse> create(@Valid @RequestBody CreateContentReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moderationService.createReport(request));
    }

    @GetMapping("/api/admin/reports")
    public ResponseEntity<PagedResponse<ContentReportResponse>> queue(
            @RequestParam(required = false) ReportStatus status, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(moderationService.listQueue(status, pageable));
    }

    @PatchMapping("/api/admin/reports/{id}/status")
    public ResponseEntity<ContentReportResponse> resolve(
            @PathVariable Long id, @Valid @RequestBody UpdateReportStatusRequest request) {
        return ResponseEntity.ok(moderationService.resolve(id, request.status()));
    }
}
