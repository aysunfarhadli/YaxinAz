package com.yaxinaz.ai;

import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spec section 34/35: malformed/unknown AI output must fall back safely rather than crash or be
 * trusted blindly. These are the pure Java rules that guarantee that - independent of whether the
 * text actually came from Claude or the mock.
 */
class AIResponseValidatorTest {

    @Test
    void unknownCategoryFallsBackToOther() {
        assertEquals(IssueCategory.OTHER, AIResponseValidator.parseCategory("not-a-real-category"));
        assertEquals(IssueCategory.OTHER, AIResponseValidator.parseCategory(null));
        assertEquals(IssueCategory.OTHER, AIResponseValidator.parseCategory(""));
    }

    @Test
    void validCategoryIsCaseInsensitive() {
        assertEquals(IssueCategory.ELEVATOR, AIResponseValidator.parseCategory("elevator"));
        assertEquals(IssueCategory.ELEVATOR, AIResponseValidator.parseCategory("Elevator"));
        assertEquals(IssueCategory.ELEVATOR, AIResponseValidator.parseCategory("ELEVATOR"));
    }

    @Test
    void unknownPriorityFallsBackToMedium() {
        assertEquals(IssuePriority.MEDIUM, AIResponseValidator.parsePriority("super-urgent"));
        assertEquals(IssuePriority.MEDIUM, AIResponseValidator.parsePriority(null));
    }

    @Test
    void requireNonBlankUsesFallbackForBlankInput() {
        assertEquals("fallback", AIResponseValidator.requireNonBlank(null, "fallback"));
        assertEquals("fallback", AIResponseValidator.requireNonBlank("   ", "fallback"));
        assertEquals("actual", AIResponseValidator.requireNonBlank("actual", "fallback"));
    }

    @Test
    void safeListFiltersBlanksAndHandlesNull() {
        assertTrue(AIResponseValidator.safeList(null).isEmpty());
        assertEquals(List.of("a", "b"), AIResponseValidator.safeList(java.util.Arrays.asList("a", "", null, "b")));
    }
}
