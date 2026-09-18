package com.yaxinaz.provider;

import org.springframework.data.jpa.domain.Specification;

public final class ProviderSpecifications {

    private ProviderSpecifications() {
    }

    public static Specification<ProviderProfile> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<ProviderProfile> hasCategory(ServiceCategory category) {
        return (root, query, cb) -> category == null ? null : cb.isMember(category, root.get("categories"));
    }

    public static Specification<ProviderProfile> isVerified(Boolean verified) {
        return (root, query, cb) -> verified == null ? null : cb.equal(root.get("verified"), verified);
    }

    public static Specification<ProviderProfile> minimumRating(Double minRating) {
        return (root, query, cb) -> minRating == null ? null : cb.greaterThanOrEqualTo(root.get("averageRating"), minRating);
    }
}
