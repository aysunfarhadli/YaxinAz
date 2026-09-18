package com.yaxinaz.ai;

import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;

import java.util.List;
import java.util.Set;

/**
 * Java-side validation of whatever the LLM returned. Nothing here trusts the model's output to be
 * well-formed - spec section 34: "Unknown category: OTHER. Unknown priority: fallback safely."
 */
public final class AIResponseValidator {

    private static final Set<String> VALID_SEARCH_RESULT_TYPES = Set.of("ISSUE", "POST", "PROVIDER", "EVENT", "LOST_FOUND");

    private AIResponseValidator() {
    }

    public static IssueCategory parseCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return IssueCategory.OTHER;
        }
        try {
            return IssueCategory.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IssueCategory.OTHER;
        }
    }

    public static IssuePriority parsePriority(String raw) {
        if (raw == null || raw.isBlank()) {
            return IssuePriority.MEDIUM;
        }
        try {
            return IssuePriority.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IssuePriority.MEDIUM;
        }
    }

    public static String requireNonBlank(String raw, String fallback) {
        return (raw == null || raw.isBlank()) ? fallback : raw.trim();
    }

    public static List<String> safeList(List<String> raw) {
        return raw == null ? List.of() : raw.stream().filter(s -> s != null && !s.isBlank()).toList();
    }

    /** Drops anything that isn't one of the search result types Java actually knows how to filter by. */
    public static List<String> safeResultTypes(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        return raw.stream()
                .filter(s -> s != null && VALID_SEARCH_RESULT_TYPES.contains(s.trim().toUpperCase()))
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .toList();
    }
}
