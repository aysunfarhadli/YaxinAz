package com.yaxinaz.analytics.dto;

import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueStatus;
import com.yaxinaz.issue.dto.IssueSummaryResponse;

import java.util.Map;

public record StatisticsResponse(
        long totalIssues,
        long openIssues,
        long resolvedIssues,
        long resolvedThisWeek,
        long criticalIssues,
        long highPriorityIssues,
        long staleIssues,
        long escalatedIssues,
        double resolutionRatePercent,
        double averageResolutionTimeHours,
        Map<IssueCategory, Long> issuesByCategory,
        Map<IssuePriority, Long> issuesByPriority,
        Map<IssueStatus, Long> issuesByStatus,
        Map<String, Long> dailyActivityLast7Days,
        IssueCategory mostCommonCategory,
        IssueSummaryResponse highestImpactUnresolvedIssue,
        long approvedMemberCount,
        long totalPosts,
        long totalEvents,
        double platformAverageProviderRating,
        long platformTotalReviews
) {
}
