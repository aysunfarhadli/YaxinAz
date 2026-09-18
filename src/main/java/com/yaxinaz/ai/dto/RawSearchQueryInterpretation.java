package com.yaxinaz.ai.dto;

import java.util.List;

/**
 * Unvalidated shape the LLM is asked to return for natural-language search. Kept separate from
 * {@link SearchQueryInterpretation} for the same reason as {@link RawIssueAIAnalysis} - Java
 * validates/normalizes before anything downstream sees it.
 */
public record RawSearchQueryInterpretation(List<String> resultTypes, String keywords) {
}
