package com.yaxinaz.ai.dto;

/**
 * Unvalidated shape the LLM is asked to return for the Announcement Assistant (spec section on
 * "Announcement Assistant"). Kept separate from {@link AnnouncementDraftResponse} for the same
 * reason as {@link RawIssueAIAnalysis} - Java validates/normalizes before anything downstream sees it.
 */
public record RawAnnouncementDraft(String title, String content) {
}
