package com.yaxinaz.review;

import com.yaxinaz.exception.DuplicateReviewException;
import com.yaxinaz.exception.InvalidServiceRequestStateException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.provider.ProviderProfile;
import com.yaxinaz.provider.ProviderProfileRepository;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.review.dto.CreateReviewRequest;
import com.yaxinaz.review.dto.ReviewResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.servicerequest.ServiceRequest;
import com.yaxinaz.servicerequest.ServiceRequestService;
import com.yaxinaz.servicerequest.ServiceRequestStatus;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final UserRepository userRepository;
    private final ServiceRequestService serviceRequestService;

    @Transactional
    public ReviewResponse createReview(Long serviceRequestId, CreateReviewRequest request) {
        ServiceRequest serviceRequest = serviceRequestService.getRequestOrThrow(serviceRequestId);
        Long userId = SecurityUtils.currentUserId();

        if (!serviceRequest.getCustomer().getId().equals(userId)) {
            throw new UnauthorizedResourceAccessException("Only the customer who requested this service can leave a review");
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.COMPLETED) {
            throw new InvalidServiceRequestStateException("You can only review a completed service request");
        }
        if (reviewRepository.existsByServiceRequestId(serviceRequestId)) {
            throw new DuplicateReviewException("You have already reviewed this service request");
        }

        User author = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));
        ProviderProfile provider = serviceRequest.getProvider();

        Review review = reviewRepository.save(Review.builder()
                .serviceRequest(serviceRequest)
                .author(author)
                .provider(provider)
                .rating(request.rating())
                .comment(request.comment())
                .build());

        recalculateProviderRating(provider, request.rating());

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> listForProvider(Long providerId, Pageable pageable) {
        Page<Review> page = reviewRepository.findAllByProviderIdOrderByCreatedAtDesc(providerId, pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    private void recalculateProviderRating(ProviderProfile provider, int newRating) {
        int oldCount = provider.getReviewCount();
        double oldAverage = provider.getAverageRating();
        int newCount = oldCount + 1;
        double newAverage = ((oldAverage * oldCount) + newRating) / newCount;

        provider.setReviewCount(newCount);
        provider.setAverageRating(Math.round(newAverage * 100.0) / 100.0);
        providerProfileRepository.save(provider);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(), review.getServiceRequest().getId(), review.getAuthor().getId(),
                review.getAuthor().getFullName(), review.getProvider().getId(), review.getRating(),
                review.getComment(), review.getCreatedAt());
    }
}
