package com.yaxinaz.ai;

import com.yaxinaz.ai.dto.AnnouncementDraftResponse;
import com.yaxinaz.ai.dto.DraftAnnouncementRequest;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.config.properties.RateLimitProperties;
import com.yaxinaz.exception.RateLimitExceededException;
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

/**
 * Announcement Assistant (Phase 20): turns a member's rough notes into a polished post draft. Pure
 * drafting aid - never persists a post itself, see {@link CommunityAIService#draftAnnouncement}.
 */
@RestController
@RequestMapping("/api/communities/{communityId}/posts")
@RequiredArgsConstructor
@Tag(name = "AI - Announcement Assistant")
public class AnnouncementAIController {

    private final CommunityAIService communityAIService;
    private final CommunityService communityService;
    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties;

    @PostMapping("/draft")
    public ResponseEntity<AnnouncementDraftResponse> draft(
            @PathVariable Long communityId, @Valid @RequestBody DraftAnnouncementRequest request) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);

        if (!rateLimiterService.tryConsume("ai-announcement:" + userId, rateLimitProperties.announcementRewritePerMinute())) {
            throw new RateLimitExceededException("Too many AI requests. Please wait a minute and try again.");
        }

        return ResponseEntity.ok(communityAIService.draftAnnouncement(
                request.rawNotes(), request.postType(), request.language()));
    }
}
