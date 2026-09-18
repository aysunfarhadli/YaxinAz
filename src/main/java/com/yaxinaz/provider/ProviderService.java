package com.yaxinaz.provider;

import com.yaxinaz.audit.AuditActionType;
import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.exception.ProviderNotFoundException;
import com.yaxinaz.exception.ProviderProfileAlreadyExistsException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.provider.dto.CreateProviderProfileRequest;
import com.yaxinaz.provider.dto.ProviderProfileResponse;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderService.class);

    private final ProviderProfileRepository providerProfileRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ProviderProfileResponse createProfile(CreateProviderProfileRequest request) {
        Long userId = SecurityUtils.currentUserId();
        if (SecurityUtils.currentRole() != Role.SERVICE_PROVIDER) {
            throw new UnauthorizedResourceAccessException("Only service provider accounts can create a provider profile");
        }
        if (providerProfileRepository.existsByUserIdAndDeletedFalse(userId)) {
            throw new ProviderProfileAlreadyExistsException("You already have a provider profile");
        }
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));

        ProviderProfile profile = providerProfileRepository.save(ProviderProfile.builder()
                .user(user)
                .businessName(request.businessName().trim())
                .bio(request.bio())
                .serviceArea(request.serviceArea())
                .categories(request.categories())
                .verified(false)
                .build());
        return toResponse(profile);
    }

    @Transactional
    public ProviderProfileResponse updateOwnProfile(CreateProviderProfileRequest request) {
        Long userId = SecurityUtils.currentUserId();
        ProviderProfile profile = providerProfileRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProviderNotFoundException(userId));
        profile.setBusinessName(request.businessName().trim());
        profile.setBio(request.bio());
        profile.setServiceArea(request.serviceArea());
        profile.setCategories(request.categories());
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProviderProfileResponse> listProviders(
            ServiceCategory category, Boolean verified, Double minRating, Pageable pageable) {
        Specification<ProviderProfile> spec = Specification.allOf(
                ProviderSpecifications.notDeleted(),
                ProviderSpecifications.hasCategory(category),
                ProviderSpecifications.isVerified(verified),
                ProviderSpecifications.minimumRating(minRating)
        );
        Page<ProviderProfile> page = providerProfileRepository.findAll(spec, pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProviderProfileResponse getProvider(Long providerId) {
        return toResponse(getProviderOrThrow(providerId));
    }

    @Transactional
    public ProviderProfileResponse setVerified(Long providerId, boolean verified) {
        if (SecurityUtils.currentRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedResourceAccessException("Only platform admins can verify service providers");
        }
        ProviderProfile profile = getProviderOrThrow(providerId);
        boolean previous = profile.isVerified();
        profile.setVerified(verified);
        log.info("PROVIDER_VERIFIED providerId={} verified={} adminId={}", providerId, verified, SecurityUtils.currentUserId());
        auditLogService.record(SecurityUtils.currentUserId(), AuditActionType.PROVIDER_VERIFIED, "ProviderProfile",
                providerId, String.valueOf(previous), String.valueOf(verified));
        return toResponse(profile);
    }

    public ProviderProfile getProviderOrThrow(Long providerId) {
        return providerProfileRepository.findByIdAndDeletedFalse(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
    }

    public ProviderProfile findOwnProfileOrThrow(Long userId) {
        return providerProfileRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProviderNotFoundException(userId));
    }

    private ProviderProfileResponse toResponse(ProviderProfile profile) {
        return new ProviderProfileResponse(
                profile.getId(), profile.getUser().getId(), profile.getBusinessName(), profile.getBio(),
                profile.getServiceArea(), profile.isVerified(), profile.getAverageRating(), profile.getReviewCount(),
                profile.getCategories(), profile.getCreatedAt());
    }
}
