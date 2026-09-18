package com.yaxinaz.ai.dto;

import com.yaxinaz.issue.dto.DuplicateCandidateResponse;

import java.util.List;

/**
 * What the "ANALYZE & REPORT" button (spec section 98) actually returns: AI's understanding of the
 * free-text report, plus Java's own duplicate-detection pass (section 39) over existing unresolved
 * issues in the same community. Two independent concerns composed at the API boundary - AI never
 * decides what counts as a duplicate.
 */
public record SmartIssueAnalysisResponse(
        IssueAIAnalysisResponse analysis,
        List<DuplicateCandidateResponse> possibleDuplicates
) {
}
