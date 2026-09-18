package com.yaxinaz.ai.dto;

import java.util.List;

/**
 * Unvalidated shape the LLM is asked to return. Category/priority are kept as raw strings
 * (never deserialized straight into the Java enums) so a slightly-off value from the model - wrong
 * case, a synonym, a typo - doesn't blow up JSON deserialization before Java gets a chance to
 * normalize/validate it (spec section 34).
 */
public record RawIssueAIAnalysis(
        String title,
        String category,
        String prioritySuggestion,
        String summary,
        List<String> affectedGroups,
        List<String> tags
) {
}
