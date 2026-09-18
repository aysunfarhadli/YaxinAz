package com.yaxinaz.ai;

import com.yaxinaz.ai.dto.AnnouncementDraftResponse;
import com.yaxinaz.ai.dto.IssueAIAnalysisResponse;
import com.yaxinaz.ai.dto.SearchQueryInterpretation;
import com.yaxinaz.analytics.dto.StatisticsResponse;
import com.yaxinaz.config.properties.AiProperties;
import com.yaxinaz.exception.AIServiceException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/**
 * The bean everything else in the application actually depends on for AI features
 * (spec sections 31/36/37): decides Claude vs Mock, times the call, records an {@link AIUsageLog}
 * row for every attempt, and falls back to the deterministic mock when Claude is disabled,
 * unconfigured, or fails and {@code yaxinaz.ai.mock-fallback} is enabled. Never silently reports
 * mock output as if it came from Claude - every response carries its real provider (either directly,
 * as {@link IssueAIAnalysisResponse#provider()}, or via the {@link AIUsageLog} row for the two
 * text-only methods).
 */
@Service
@Primary
@RequiredArgsConstructor
public class CommunityAIServiceRouter implements CommunityAIService {

    private static final String FEATURE_ISSUE_ANALYSIS = "ISSUE_ANALYSIS";
    private static final String FEATURE_COMMUNITY_SUMMARY = "COMMUNITY_SUMMARY";
    private static final String FEATURE_COMMUNITY_INSIGHTS = "COMMUNITY_INSIGHTS";
    private static final String FEATURE_ANNOUNCEMENT_DRAFT = "ANNOUNCEMENT_DRAFT";
    private static final String FEATURE_SEARCH_QUERY = "SEARCH_QUERY_INTERPRETATION";

    private static final Logger log = LoggerFactory.getLogger(CommunityAIServiceRouter.class);

    private final ClaudeCommunityAIService claudeCommunityAIService;
    private final MockCommunityAIService mockCommunityAIService;
    private final AiProperties aiProperties;
    private final AIUsageLogRepository aiUsageLogRepository;

    @Override
    public IssueAIAnalysisResponse analyzeIssue(String rawText, String buildingOrLocation, String language) {
        return route(FEATURE_ISSUE_ANALYSIS,
                () -> claudeCommunityAIService.analyzeIssue(rawText, buildingOrLocation, language),
                () -> mockCommunityAIService.analyzeIssue(rawText, buildingOrLocation, language));
    }

    @Override
    public String summarizeCommunity(StatisticsResponse stats, String communityName, String language) {
        return route(FEATURE_COMMUNITY_SUMMARY,
                () -> claudeCommunityAIService.summarizeCommunity(stats, communityName, language),
                () -> mockCommunityAIService.summarizeCommunity(stats, communityName, language));
    }

    @Override
    public List<String> generateInsights(StatisticsResponse stats, String language) {
        return route(FEATURE_COMMUNITY_INSIGHTS,
                () -> claudeCommunityAIService.generateInsights(stats, language),
                () -> mockCommunityAIService.generateInsights(stats, language));
    }

    @Override
    public AnnouncementDraftResponse draftAnnouncement(String rawNotes, String postType, String language) {
        return route(FEATURE_ANNOUNCEMENT_DRAFT,
                () -> claudeCommunityAIService.draftAnnouncement(rawNotes, postType, language),
                () -> mockCommunityAIService.draftAnnouncement(rawNotes, postType, language));
    }

    @Override
    public SearchQueryInterpretation interpretSearchQuery(String query, String language) {
        return route(FEATURE_SEARCH_QUERY,
                () -> claudeCommunityAIService.interpretSearchQuery(query, language),
                () -> mockCommunityAIService.interpretSearchQuery(query, language));
    }

    /**
     * Cheap, configuration-based read for the System Health widget (Phase 13) - deliberately does
     * NOT make a live Claude call on every health check (that would burn API quota just for a
     * dashboard refresh). Reflects the most recent real outcome recorded in {@link AIUsageLog}
     * instead.
     */
    public AIProvider currentMode() {
        if (!aiProperties.enabled()) {
            return AIProvider.MOCK;
        }
        return aiUsageLogRepository.findTopByFeatureOrderByCreatedAtDesc(FEATURE_ISSUE_ANALYSIS)
                .map(AIUsageLog::getProvider)
                .orElse(AIProvider.CLAUDE);
    }

    private <T> T route(String feature, Supplier<T> claudeCall, Supplier<T> mockCall) {
        if (!aiProperties.enabled()) {
            return runMock(feature, mockCall, "AI disabled by configuration");
        }

        long start = System.currentTimeMillis();
        try {
            T result = claudeCall.get();
            logUsage(feature, AIProvider.CLAUDE, AIUsageStatus.SUCCESS, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            logUsage(feature, AIProvider.CLAUDE, AIUsageStatus.FAILURE, System.currentTimeMillis() - start);
            log.warn("Claude {} failed, falling back per configuration: {}", feature, ex.getMessage());

            if (!aiProperties.mockFallback()) {
                throw new AIServiceException("AI assistance is temporarily unavailable. You can still continue manually.", ex);
            }
            return runMock(feature, mockCall, "Claude failed: " + ex.getMessage());
        }
    }

    private <T> T runMock(String feature, Supplier<T> mockCall, String reason) {
        long start = System.currentTimeMillis();
        T result = mockCall.get();
        logUsage(feature, AIProvider.MOCK, AIUsageStatus.FALLBACK_USED, System.currentTimeMillis() - start);
        log.info("AI_MOCK_USED feature={} reason={}", feature, reason);
        return result;
    }

    private void logUsage(String feature, AIProvider provider, AIUsageStatus status, long latencyMs) {
        aiUsageLogRepository.save(AIUsageLog.builder()
                .feature(feature)
                .provider(provider)
                .model(aiProperties.model())
                .status(status)
                .latencyMs(latencyMs)
                .build());
    }
}
