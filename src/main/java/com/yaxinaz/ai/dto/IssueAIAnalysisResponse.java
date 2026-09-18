package com.yaxinaz.ai.dto;

import com.yaxinaz.ai.AIProvider;
import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;

import java.util.List;

public record IssueAIAnalysisResponse(
        String title,
        IssueCategory category,
        IssuePriority prioritySuggestion,
        String summary,
        List<String> affectedGroups,
        List<String> tags,
        AIProvider provider
) {
}
