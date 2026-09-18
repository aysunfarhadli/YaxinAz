package com.yaxinaz.ai;

import com.yaxinaz.ai.dto.AnnouncementDraftResponse;
import com.yaxinaz.ai.dto.IssueAIAnalysisResponse;
import com.yaxinaz.ai.dto.SearchQueryInterpretation;
import com.yaxinaz.analytics.dto.StatisticsResponse;
import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockCommunityAIServiceTest {

    private final MockCommunityAIService service = new MockCommunityAIService();

    @Test
    void detectsElevatorIssueWithElderlyImpactAsHighPriority() {
        IssueAIAnalysisResponse response = service.analyzeIssue(
                "The elevator in Building B has stopped working again and several elderly residents cannot easily use the stairs.",
                "Building B", "EN");

        assertEquals(IssueCategory.ELEVATOR, response.category());
        assertEquals(IssuePriority.HIGH, response.prioritySuggestion());
        assertTrue(response.affectedGroups().contains("ELDERLY"));
        assertEquals(AIProvider.MOCK, response.provider());
    }

    @Test
    void detectsWaterLeakAsWaterCategory() {
        IssueAIAnalysisResponse response = service.analyzeIssue(
                "There is a water leak from the pipe in the basement.", null, "EN");

        assertEquals(IssueCategory.WATER, response.category());
    }

    @Test
    void unrecognizedTextFallsBackToOtherAndMediumPriority() {
        IssueAIAnalysisResponse response = service.analyzeIssue(
                "Something strange happened in the common area yesterday evening.", null, "EN");

        assertEquals(IssueCategory.OTHER, response.category());
        assertEquals(IssuePriority.MEDIUM, response.prioritySuggestion());
    }

    @Test
    void criticalKeywordsProduceCriticalPriority() {
        IssueAIAnalysisResponse response = service.analyzeIssue(
                "Residents report smoke and a possible gas leak near the parking garage.", null, "EN");

        assertEquals(IssuePriority.CRITICAL, response.prioritySuggestion());
    }

    @Test
    void insightsAreEmptyWhenThereIsInsufficientData() {
        StatisticsResponse stats = statsWithTotal(2);
        assertTrue(service.generateInsights(stats, "EN").isEmpty());
    }

    @Test
    void insightsFlagEscalatedIssuesWhenPresent() {
        StatisticsResponse stats = new StatisticsResponse(
                10, 6, 4, 1, 0, 2, 1, 2, 40.0, 12.0,
                Map.of(IssueCategory.ELEVATOR, 5L), Map.of(), Map.of(), Map.of(),
                IssueCategory.ELEVATOR, null, 20, 3, 1, 0.0, 0);

        List<String> insights = service.generateInsights(stats, "EN");
        assertTrue(insights.stream().anyMatch(s -> s.contains("escalated")));
    }

    @Test
    void summaryNeverInventsNumbersNotInStats() {
        StatisticsResponse stats = statsWithTotal(0);
        String summary = service.summarizeCommunity(stats, "Green Park Residence", "EN");
        assertTrue(summary.contains("Green Park Residence"));
        assertTrue(summary.toLowerCase().contains("no issues"));
    }

    @Test
    void draftAnnouncementCleansUpNotesWithoutInventingFacts() {
        AnnouncementDraftResponse draft = service.draftAnnouncement(
                "water will be shut off tomorrow 10am to 2pm for maintenance", "ALERT", "EN");

        assertTrue(draft.title().startsWith("Alert:"));
        assertTrue(draft.content().toLowerCase().contains("water will be shut off tomorrow 10am to 2pm for maintenance"));
        assertEquals(AIProvider.MOCK, draft.provider());
    }

    @Test
    void interpretSearchQueryInfersProviderTypeAndStripsFillerWords() {
        SearchQueryInterpretation result = service.interpretSearchQuery("find a plumber near me", "EN");

        assertTrue(result.resultTypes().contains("PROVIDER"));
        assertTrue(result.keywords().contains("plumber"));
        assertTrue(!result.keywords().contains("find "));
        assertEquals(AIProvider.MOCK, result.provider());
    }

    @Test
    void interpretSearchQueryInfersLostFoundType() {
        SearchQueryInterpretation result = service.interpretSearchQuery("has anyone found a lost cat", "EN");

        assertTrue(result.resultTypes().contains("LOST_FOUND"));
    }

    @Test
    void interpretSearchQueryReturnsEmptyTypesWhenNothingMatchesKeywordTable() {
        SearchQueryInterpretation result = service.interpretSearchQuery("xyzabc123", "EN");

        assertTrue(result.resultTypes().isEmpty());
        assertEquals("xyzabc123", result.keywords());
    }

    private StatisticsResponse statsWithTotal(long total) {
        return new StatisticsResponse(
                total, total, 0, 0, 0, 0, 0, 0, 0.0, 0.0,
                Map.of(), Map.of(), Map.of(IssueStatus.OPEN, total), Map.of(),
                total > 0 ? IssueCategory.OTHER : null, null, 5, 0, 0, 0.0, 0);
    }
}
