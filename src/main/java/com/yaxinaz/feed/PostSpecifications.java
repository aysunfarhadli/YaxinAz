package com.yaxinaz.feed;

import org.springframework.data.jpa.domain.Specification;

public final class PostSpecifications {

    private PostSpecifications() {
    }

    public static Specification<Post> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Post> inCommunity(Long communityId) {
        return (root, query, cb) -> cb.equal(root.get("community").get("id"), communityId);
    }

    public static Specification<Post> hasType(PostType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("postType"), type);
    }
}
