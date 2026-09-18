package com.yaxinaz.review;

import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.review.dto.CreateReviewRequest;
import com.yaxinaz.review.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/service-requests/{id}/reviews")
    public ResponseEntity<ReviewResponse> create(@PathVariable Long id, @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(id, request));
    }

    @GetMapping("/api/providers/{providerId}/reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> listForProvider(
            @PathVariable Long providerId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.listForProvider(providerId, pageable));
    }
}
