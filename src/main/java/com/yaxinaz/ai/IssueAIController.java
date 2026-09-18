package com.yaxinaz.ai;

import com.yaxinaz.ai.dto.AnalyzeIssueRequest;
import com.yaxinaz.ai.dto.IssueAIAnalysisResponse;
import com.yaxinaz.ai.dto.SmartIssueAnalysisResponse;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.config.properties.RateLimitProperties;
import com.yaxinaz.exception.RateLimitExceededException;
import com.yaxinaz.issue.DuplicateDetectionService;
import com.yaxinaz.issue.dto.DuplicateCandidateResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.security.ratelimit.RateLimiterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communities/{communityId}/issues")
@RequiredArgsConstructor
@Tag(name = "AI - Issue Analysis")
public class IssueAIController {

    private final CommunityAIService communityAIService;
    private final CommunityService communityService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties;

    @PostMapping("/analyze")
    public ResponseEntity<SmartIssueAnalysisResponse> analyze(
            @PathVariable Long communityId, @Valid @RequestBody AnalyzeIssueRequest request) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);

        if (!rateLimiterService.tryConsume("ai:" + userId, rateLimitProperties.aiRequestsPerMinute())) {
            throw new RateLimitExceededException("Too many AI requests. Please wait a minute and try again.");
        }

        IssueAIAnalysisResponse analysis = communityAIService.analyzeIssue(
                request.rawText(), request.buildingOrLocation(), request.language());

        List<DuplicateCandidateResponse> duplicates = duplicateDetectionService.findCandidates(
                communityId, analysis.category(), request.buildingOrLocation(), request.rawText());

        return ResponseEntity.ok(new SmartIssueAnalysisResponse(analysis, duplicates));
    }
}
