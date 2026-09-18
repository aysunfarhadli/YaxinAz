package com.yaxinaz.ai;

import com.yaxinaz.ai.dto.AnnouncementDraftResponse;
import com.yaxinaz.ai.dto.IssueAIAnalysisResponse;
import com.yaxinaz.ai.dto.SearchQueryInterpretation;
import com.yaxinaz.analytics.dto.StatisticsResponse;

import java.util.List;

/**
 * The single seam between "AI understands language" and "Java controls the system" (spec section
 * 1). Implementations MUST NOT persist entities, authorize users, or make final business decisions
 * - they only turn natural language (or, for the summary/insights methods, real Java-computed
 * numbers) into human-readable text. Grows one method per AI-powered feature across later phases -
 * each new method must be implemented by both {@link com.yaxinaz.ai.ClaudeCommunityAIService} and
 * {@link com.yaxinaz.ai.MockCommunityAIService}.
 */
public interface CommunityAIService {

    IssueAIAnalysisResponse analyzeIssue(String rawText, String buildingOrLocation, String language);

    /**
     * Announcement Assistant (Phase 20): turns a resident's rough notes into a polished title +
     * body for a community post. Purely a drafting aid - the caller still has to explicitly submit
     * the (editable) result via the normal post-creation endpoint; nothing here persists a post.
     */
    AnnouncementDraftResponse draftAnnouncement(String rawNotes, String postType, String language);

    /**
     * Turns an already-computed {@link StatisticsResponse} into a short prose summary. The
     * implementation must never invent numbers not present in {@code stats} (spec section 59).
     */
    String summarizeCommunity(StatisticsResponse stats, String communityName, String language);

    /**
     * Returns 0-3 short insight sentences derived strictly from {@code stats}. Must return an empty
     * list rather than fabricate a trend when there isn't enough data to support one (spec section
     * 60's "insufficient data -> do not create unsupported insight").
     */
    List<String> generateInsights(StatisticsResponse stats, String language);

    /**
     * Natural-language search assist: turns a free-text query into which result types are actually
     * relevant and a cleaned-up keyword phrase. AI never returns search results itself - it only
     * narrows what {@link com.yaxinaz.search.SearchService} then runs through the real database
     * queries, so results can never be fabricated (same principle as {@link #analyzeIssue}).
     */
    SearchQueryInterpretation interpretSearchQuery(String query, String language);
}
