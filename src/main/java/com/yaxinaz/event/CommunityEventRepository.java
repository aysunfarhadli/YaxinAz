package com.yaxinaz.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CommunityEventRepository extends JpaRepository<CommunityEvent, Long> {

    Optional<CommunityEvent> findByIdAndCommunityIdAndDeletedFalse(Long id, Long communityId);

    Page<CommunityEvent> findAllByCommunityIdAndDeletedFalseAndStartTimeAfterOrderByStartTimeAsc(
            Long communityId, Instant after, Pageable pageable);

    Page<CommunityEvent> findAllByCommunityIdAndDeletedFalseAndStartTimeBeforeOrderByStartTimeDesc(
            Long communityId, Instant before, Pageable pageable);

    Page<CommunityEvent> findAllByCommunityIdAndDeletedFalseOrderByStartTimeAsc(Long communityId, Pageable pageable);

    long countByCommunityIdAndDeletedFalse(Long communityId);

    @Query("select e from CommunityEvent e where e.community.id = :communityId and e.deleted = false "
            + "and (lower(e.title) like lower(concat('%', :q, '%')) or lower(coalesce(e.description, '')) like lower(concat('%', :q, '%')))")
    List<CommunityEvent> searchByKeyword(@Param("communityId") Long communityId, @Param("q") String query, Pageable pageable);
}
