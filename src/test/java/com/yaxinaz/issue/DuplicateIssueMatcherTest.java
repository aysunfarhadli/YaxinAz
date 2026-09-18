package com.yaxinaz.issue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DuplicateIssueMatcherTest {

    @Test
    void nearIdenticalReportsScoreAboveThreshold() {
        double score = DuplicateIssueMatcher.score(
                "Building B", "The elevator in Building B has stopped working and is unsafe.",
                "Building B", "Elevator in Building B stopped working again, unsafe for elderly residents.");

        assertTrue(score >= DuplicateIssueMatcher.SIMILARITY_THRESHOLD,
                "expected score >= threshold but was " + score);
    }

    @Test
    void unrelatedReportsScoreBelowThreshold() {
        double score = DuplicateIssueMatcher.score(
                "Building B", "The elevator in Building B has stopped working.",
                "Building A", "There is loud noise coming from the parking garage at night.");

        assertTrue(score < DuplicateIssueMatcher.SIMILARITY_THRESHOLD,
                "expected score < threshold but was " + score);
    }

    @Test
    void sameLocationDifferentTopicScoresBelowThreshold() {
        double score = DuplicateIssueMatcher.score(
                "Building B", "The elevator in Building B has stopped working.",
                "Building B", "There is a water leak near the entrance of Building B.");

        assertTrue(score < DuplicateIssueMatcher.SIMILARITY_THRESHOLD,
                "expected score < threshold but was " + score);
    }
}
