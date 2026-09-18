package com.yaxinaz.community;

import com.yaxinaz.audit.AuditActionType;
import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.community.dto.CommunityMemberResponse;
import com.yaxinaz.community.dto.CommunityResponse;
import com.yaxinaz.community.dto.CreateCommunityRequest;
import com.yaxinaz.community.event.MembershipStatusChangedEvent;
import com.yaxinaz.exception.CommunityNotFoundException;
import com.yaxinaz.exception.DuplicateMembershipException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.exception.UserNotFoundException;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private static final Logger log = LoggerFactory.getLogger(CommunityService.class);

    private final CommunityRepository communityRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final CommunityMapper communityMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    @Transactional
    public CommunityResponse createCommunity(CreateCommunityRequest request) {
        Role role = SecurityUtils.currentRole();
        if (role != Role.COMMUNITY_ADMIN && role != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedResourceAccessException("Only community admins can create communities");
        }

        Long userId = SecurityUtils.currentUserId();
        Community community = Community.builder()
                .name(request.name().trim())
                .description(request.description())
                .type(request.type())
                .city(request.city().trim())
                .district(request.district())
                .address(request.address())
                .coverImageUrl(request.coverImageUrl())
                .createdBy(userId)
                .build();
        community = communityRepository.save(community);

        // The creator is automatically an approved member of their own community - otherwise a
        // COMMUNITY_ADMIN couldn't post/poll/report issues in the community they just created,
        // since every one of those actions requires approved membership, not just admin ownership.
        User creator = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));
        membershipRepository.save(CommunityMembership.builder()
                .user(creator)
                .community(community)
                .status(MembershipStatus.APPROVED)
                .joinedAt(Instant.now())
                .reviewedAt(Instant.now())
                .reviewedBy(userId)
                .build());

        return withMemberCount(community);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommunityResponse> listCommunities(String city, Pageable pageable) {
        var page = (city == null || city.isBlank())
                ? communityRepository.findAllByDeletedFalse(pageable)
                : communityRepository.findAllByDeletedFalseAndCityIgnoreCase(city, pageable);
        return PagedResponse.of(page, this::withMemberCount);
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunity(Long communityId) {
        return withMemberCount(getCommunityOrThrow(communityId));
    }

    /** Communities the current user is an approved member of - powers the frontend's community selector. */
    @Transactional(readOnly = true)
    public java.util.List<CommunityResponse> listMyCommunities() {
        Long userId = SecurityUtils.currentUserId();
        return membershipRepository.findAllByUserIdAndStatus(userId, MembershipStatus.APPROVED).stream()
                .map(membership -> withMemberCount(membership.getCommunity()))
                .toList();
    }

    @Transactional
    public CommunityMemberResponse requestToJoin(Long communityId) {
        Community community = getCommunityOrThrow(communityId);
        Long userId = SecurityUtils.currentUserId();

        membershipRepository.findByUserIdAndCommunityId(userId, communityId).ifPresent(existing -> {
            throw new DuplicateMembershipException(
                    "You already have a " + existing.getStatus() + " membership request for this community");
        });

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        CommunityMembership membership = CommunityMembership.builder()
                .user(user)
                .community(community)
                .status(MembershipStatus.PENDING)
                .joinedAt(Instant.now())
                .build();
        membership = membershipRepository.save(membership);
        log.info("MEMBERSHIP_REQUESTED userId={} communityId={}", userId, communityId);
        return communityMapper.toMemberResponse(membership);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommunityMemberResponse> listMembers(Long communityId, MembershipStatus status, Pageable pageable) {
        Community community = getCommunityOrThrow(communityId);
        MembershipStatus effectiveStatus = status != null ? status : MembershipStatus.APPROVED;
        if (effectiveStatus != MembershipStatus.APPROVED) {
            requireCommunityAdmin(community);
        }
        var page = membershipRepository.findAllByCommunityIdAndStatus(communityId, effectiveStatus, pageable);
        return PagedResponse.of(page, communityMapper::toMemberResponse);
    }

    @Transactional
    public CommunityMemberResponse reviewMembership(Long communityId, Long membershipId, MembershipStatus newStatus) {
        Community community = getCommunityOrThrow(communityId);
        requireCommunityAdmin(community);

        CommunityMembership membership = membershipRepository.findById(membershipId)
                .filter(m -> m.getCommunity().getId().equals(communityId))
                .orElseThrow(() -> new CommunityNotFoundException(communityId));

        MembershipStatus previousStatus = membership.getStatus();
        membership.setStatus(newStatus);
        membership.setReviewedAt(Instant.now());
        membership.setReviewedBy(SecurityUtils.currentUserId());
        log.info("MEMBERSHIP_{} membershipId={} communityId={} reviewedBy={}",
                newStatus, membershipId, communityId, SecurityUtils.currentUserId());

        AuditActionType auditAction = switch (newStatus) {
            case APPROVED -> AuditActionType.MEMBERSHIP_APPROVED;
            case REJECTED -> AuditActionType.MEMBERSHIP_REJECTED;
            case BLOCKED -> AuditActionType.MEMBERSHIP_BLOCKED;
            case PENDING -> null;
        };
        if (auditAction != null) {
            auditLogService.record(SecurityUtils.currentUserId(), auditAction, "CommunityMembership",
                    membershipId, previousStatus.name(), newStatus.name());
        }

        eventPublisher.publishEvent(new MembershipStatusChangedEvent(
                membership.getUser().getId(), community.getId(), community.getName(), newStatus));
        return communityMapper.toMemberResponse(membership);
    }

    public boolean isApprovedMember(Long userId, Long communityId) {
        return membershipRepository.existsByUserIdAndCommunityIdAndStatus(userId, communityId, MembershipStatus.APPROVED);
    }

    public void requireApprovedMember(Long userId, Long communityId) {
        if (!isApprovedMember(userId, communityId)) {
            throw new UnauthorizedResourceAccessException("You must be an approved member of this community to access this resource");
        }
    }

    public void requireCommunityAdmin(Community community) {
        Role role = SecurityUtils.currentRole();
        Long userId = SecurityUtils.currentUserId();
        boolean isPlatformAdmin = role == Role.PLATFORM_ADMIN;
        boolean isOwningCommunityAdmin = role == Role.COMMUNITY_ADMIN && community.getCreatedBy().equals(userId);
        if (!isPlatformAdmin && !isOwningCommunityAdmin) {
            throw new UnauthorizedResourceAccessException("You do not have admin permissions for this community");
        }
    }

    public Community getCommunityOrThrow(Long communityId) {
        return communityRepository.findByIdAndDeletedFalse(communityId)
                .orElseThrow(() -> new CommunityNotFoundException(communityId));
    }

    private CommunityResponse withMemberCount(Community community) {
        long memberCount = membershipRepository.countByCommunityIdAndStatus(community.getId(), MembershipStatus.APPROVED);
        CommunityResponse base = communityMapper.toResponse(community);
        return new CommunityResponse(
                base.id(), base.name(), base.description(), base.type(), base.city(),
                base.district(), base.address(), base.coverImageUrl(), memberCount, base.createdAt()
        );
    }
}
