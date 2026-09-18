package com.yaxinaz.issue;

import com.yaxinaz.issue.dto.CommentResponse;
import com.yaxinaz.issue.dto.CreateCommentRequest;
import com.yaxinaz.issue.dto.CreateIssueRequest;
import com.yaxinaz.issue.dto.DuplicateCandidateResponse;
import com.yaxinaz.issue.dto.IssueActivityResponse;
import com.yaxinaz.issue.dto.IssueResponse;
import com.yaxinaz.issue.dto.IssueSummaryResponse;
import com.yaxinaz.issue.dto.UpdateIssuePriorityRequest;
import com.yaxinaz.issue.dto.UpdateIssueStatusRequest;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Tag(name = "Issues")
public class IssueController {

    private final IssueService issueService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final CommunityService communityService;

    @PostMapping("/api/communities/{communityId}/issues")
    public ResponseEntity<IssueResponse> create(@PathVariable Long communityId, @Valid @RequestBody CreateIssueRequest request) {
        CreateIssueRequest effective = new CreateIssueRequest(
                communityId, request.title(), request.description(), request.category(),
                request.priority(), request.buildingOrLocation(), request.imageUrl(), request.aiSummary());
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.createIssue(effective));
    }

    @GetMapping("/api/communities/{communityId}/issues")
    public ResponseEntity<PagedResponse<IssueSummaryResponse>> list(
            @PathVariable Long communityId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) IssueCategory category,
            @RequestParam(required = false) Boolean stale,
            @RequestParam(required = false) Boolean escalated,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(issueService.listIssues(
                communityId, status, priority, category, stale, escalated, from, to, pageable));
    }

    @GetMapping("/api/issues/{id}")
    public ResponseEntity<IssueResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssue(id));
    }

    @GetMapping("/api/communities/{communityId}/issues/similar")
    public ResponseEntity<java.util.List<DuplicateCandidateResponse>> findSimilar(
            @PathVariable Long communityId,
            @RequestParam IssueCategory category,
            @RequestParam(required = false) String buildingOrLocation,
            @RequestParam(required = false) String text) {
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), communityId);
        return ResponseEntity.ok(duplicateDetectionService.findCandidates(communityId, category, buildingOrLocation, text));
    }

    @PatchMapping("/api/issues/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateIssueStatusRequest request) {
        return ResponseEntity.ok(issueService.updateStatus(id, request.status()));
    }

    @PatchMapping("/api/issues/{id}/priority")
    public ResponseEntity<IssueResponse> updatePriority(@PathVariable Long id, @Valid @RequestBody UpdateIssuePriorityRequest request) {
        return ResponseEntity.ok(issueService.updatePriority(id, request.priority()));
    }

    @PostMapping("/api/issues/{id}/support")
    public ResponseEntity<Void> support(@PathVariable Long id) {
        issueService.supportIssue(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/api/issues/{id}/timeline")
    public ResponseEntity<java.util.List<IssueActivityResponse>> timeline(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getTimeline(id));
    }

    @PostMapping("/api/issues/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.addComment(id, request));
    }

    @GetMapping("/api/issues/{id}/comments")
    public ResponseEntity<PagedResponse<CommentResponse>> listComments(
            @PathVariable Long id, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(issueService.listComments(id, pageable));
    }
}
