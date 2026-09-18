package com.yaxinaz.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, Long> {

    Optional<CommunityMembership> findByUserIdAndCommunityId(Long userId, Long communityId);

    boolean existsByUserIdAndCommunityIdAndStatus(Long userId, Long communityId, MembershipStatus status);

    Page<CommunityMembership> findAllByCommunityIdAndStatus(Long communityId, MembershipStatus status, Pageable pageable);

    Page<CommunityMembership> findAllByUserId(Long userId, Pageable pageable);

    List<CommunityMembership> findAllByUserIdAndStatus(Long userId, MembershipStatus status);

    long countByCommunityIdAndStatus(Long communityId, MembershipStatus status);

    long countByStatus(MembershipStatus status);
}
