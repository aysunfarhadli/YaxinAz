package com.yaxinaz.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByServiceRequestId(Long serviceRequestId);

    Page<Review> findAllByProviderIdOrderByCreatedAtDesc(Long providerId, Pageable pageable);
}
