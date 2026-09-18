package com.yaxinaz.lostfound;

import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.exception.LostFoundItemNotFoundException;
import com.yaxinaz.lostfound.dto.CreateLostFoundItemRequest;
import com.yaxinaz.lostfound.dto.LostFoundItemResponse;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostFoundService {

    private final LostFoundRepository lostFoundRepository;
    private final UserRepository userRepository;
    private final CommunityService communityService;

    @Transactional
    public LostFoundItemResponse createItem(Long communityId, CreateLostFoundItemRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Community community = communityService.getCommunityOrThrow(communityId);
        communityService.requireApprovedMember(userId, communityId);

        LostFoundItem item = lostFoundRepository.save(LostFoundItem.builder()
                .community(community)
                .type(request.type())
                .title(request.title().trim())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .location(request.location())
                .status(LostFoundStatus.ACTIVE)
                .createdBy(userId)
                .build());

        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LostFoundItemResponse> listItems(
            Long communityId, LostFoundType type, LostFoundStatus status, Pageable pageable) {
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), communityId);

        Specification<LostFoundItem> spec = Specification.allOf(
                LostFoundSpecifications.notDeleted(),
                LostFoundSpecifications.inCommunity(communityId),
                LostFoundSpecifications.hasType(type),
                LostFoundSpecifications.hasStatus(status)
        );
        Page<LostFoundItem> page = lostFoundRepository.findAll(spec, pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public LostFoundItemResponse getItem(Long communityId, Long itemId) {
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), communityId);
        return toResponse(getItemOrThrow(communityId, itemId));
    }

    @Transactional
    public LostFoundItemResponse updateStatus(Long communityId, Long itemId, LostFoundStatus status) {
        LostFoundItem item = getItemOrThrow(communityId, itemId);
        Long userId = SecurityUtils.currentUserId();
        if (!item.getCreatedBy().equals(userId)) {
            communityService.requireCommunityAdmin(item.getCommunity());
        }
        item.setStatus(status);
        return toResponse(item);
    }

    private LostFoundItem getItemOrThrow(Long communityId, Long itemId) {
        return lostFoundRepository.findByIdAndCommunityIdAndDeletedFalse(itemId, communityId)
                .orElseThrow(() -> new LostFoundItemNotFoundException(itemId));
    }

    private LostFoundItemResponse toResponse(LostFoundItem item) {
        String reporterName = userRepository.findByIdAndDeletedFalse(item.getCreatedBy())
                .map(User::getFullName)
                .orElse("Unknown resident");
        return new LostFoundItemResponse(
                item.getId(), item.getCommunity().getId(), item.getType(), item.getTitle(), item.getDescription(),
                item.getImageUrl(), item.getLocation(), item.getStatus(), item.getCreatedBy(), reporterName,
                item.getCreatedAt());
    }
}
