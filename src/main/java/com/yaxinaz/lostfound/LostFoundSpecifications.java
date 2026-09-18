package com.yaxinaz.lostfound;

import org.springframework.data.jpa.domain.Specification;

public final class LostFoundSpecifications {

    private LostFoundSpecifications() {
    }

    public static Specification<LostFoundItem> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<LostFoundItem> inCommunity(Long communityId) {
        return (root, query, cb) -> cb.equal(root.get("community").get("id"), communityId);
    }

    public static Specification<LostFoundItem> hasType(LostFoundType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<LostFoundItem> hasStatus(LostFoundStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
