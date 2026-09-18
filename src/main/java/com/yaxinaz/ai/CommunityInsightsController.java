package com.yaxinaz.ai;

import com.yaxinaz.analytics.AnalyticsService;
import com.yaxinaz.analytics.dto.StatisticsResponse;
import com.yaxinaz.community.CommunityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI narration built strictly on top of Java-computed {@link StatisticsResponse} numbers - the AI
 * never sees raw database access and cannot report a figure {@link AnalyticsService} didn't already
 * compute (spec sections 59-60).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "AI - Community Insights")
public class CommunityInsightsController {

    private final AnalyticsService analyticsService;
    private final CommunityAIService communityAIService;
    private final CommunityService communityService;

    @GetMapping("/api/communities/{id}/summary")
    public ResponseEntity<Map<String, String>> summary(
            @PathVariable Long id, @RequestParam(required = false, defaultValue = "EN") String language) {
        StatisticsResponse stats = analyticsService.computeStatistics(id);
        String communityName = communityService.getCommunity(id).name();
        String summary = communityAIService.summarizeCommunity(stats, communityName, language);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    @GetMapping("/api/communities/{id}/insights")
    public ResponseEntity<List<String>> insights(
            @PathVariable Long id, @RequestParam(required = false, defaultValue = "EN") String language) {
        StatisticsResponse stats = analyticsService.computeStatistics(id);
        return ResponseEntity.ok(communityAIService.generateInsights(stats, language));
    }
}
