package com.yaxinaz.issue;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public final class IssueSpecifications {

    private IssueSpecifications() {
    }

    public static Specification<Issue> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Issue> inCommunity(Long communityId) {
        return (root, query, cb) -> communityId == null ? null : cb.equal(root.get("community").get("id"), communityId);
    }

    public static Specification<Issue> hasStatus(IssueStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Issue> statusIn(List<IssueStatus> statuses) {
        return (root, query, cb) -> (statuses == null || statuses.isEmpty()) ? null : root.get("status").in(statuses);
    }

    public static Specification<Issue> hasPriority(IssuePriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Issue> hasCategory(IssueCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Issue> isStale(Boolean stale) {
        return (root, query, cb) -> stale == null ? null : cb.equal(root.get("stale"), stale);
    }

    public static Specification<Issue> isEscalated(Boolean escalated) {
        return (root, query, cb) -> escalated == null ? null : cb.equal(root.get("escalated"), escalated);
    }

    public static Specification<Issue> createdAfter(Instant from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Issue> createdBefore(Instant to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
