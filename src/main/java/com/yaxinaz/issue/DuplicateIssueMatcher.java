package com.yaxinaz.issue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Pure, deterministic similarity scoring for spec section 39 (duplicate issue detection). No AI
 * involved by default - this is a plain Java heuristic (location match + keyword overlap) that runs
 * whether or not Claude is reachable, so duplicate checking keeps working even when AI is down
 * (section 37's "core application must still work" applies here too). Kept as static/pure methods
 * so it's trivially unit-testable without a database or Spring context.
 */
public final class DuplicateIssueMatcher {

    /** Below this combined score, two reports are not considered likely duplicates. */
    public static final double SIMILARITY_THRESHOLD = 0.34;

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "is", "are", "was", "were", "in", "on", "at", "to", "of", "and", "or",
            "again", "has", "have", "had", "it", "its", "this", "that", "there", "with", "for", "by",
            "residents", "resident", "building", "problem", "issue", "please", "not", "no", "from");

    private DuplicateIssueMatcher() {
    }

    /**
     * Keyword overlap carries the score; a matching location only boosts an already-similar report.
     * Two reports in the same building about completely different topics must NOT score as
     * duplicates just because they share a location - so location alone (with zero keyword overlap)
     * always yields 0.
     */
    public static double score(String locationA, String textA, String locationB, String textB) {
        double locationScore = locationScore(locationA, locationB);
        double keywordScore = keywordScore(textA, textB);
        return Math.min(1.0, keywordScore * (1 + (0.5 * locationScore)));
    }

    static double locationScore(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        if (na.isEmpty() || nb.isEmpty()) {
            return 0.0;
        }
        if (na.equals(nb)) {
            return 1.0;
        }
        if (na.contains(nb) || nb.contains(na)) {
            return 0.6;
        }
        return 0.0;
    }

    static double keywordScore(String a, String b) {
        Set<String> tokensA = tokenize(a);
        Set<String> tokensB = tokenize(b);
        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(tokensA);
        intersection.retainAll(tokensB);
        Set<String> union = new HashSet<>(tokensA);
        union.addAll(tokensB);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private static Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        Set<String> tokens = new HashSet<>();
        for (String word : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
