package com.yaxinaz.issue;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {

    Optional<Issue> findByIdAndDeletedFalse(Long id);

    List<Issue> findAllByCommunityIdAndStatusInAndDeletedFalse(Long communityId, List<IssueStatus> statuses);

    List<Issue> findAllByCommunityIdAndDeletedFalse(Long communityId);

    @Query("select i from Issue i where i.community.id = :communityId and i.deleted = false "
            + "and (lower(i.title) like lower(concat('%', :q, '%')) or lower(i.description) like lower(concat('%', :q, '%')))")
    List<Issue> searchByKeyword(@Param("communityId") Long communityId, @Param("q") String query, Pageable pageable);

    List<Issue> findAllByPriorityInAndStatusInAndUpdatedAtBeforeAndStaleFalseAndDeletedFalse(
            List<IssuePriority> priorities, List<IssueStatus> statuses, Instant updatedBefore);

    List<Issue> findAllByPriorityAndStatusAndCreatedAtBeforeAndEscalatedFalseAndDeletedFalse(
            IssuePriority priority, IssueStatus status, Instant createdBefore);

    List<Issue> findAllByPriorityAndCreatedAtBeforeAndEscalatedFalseAndDeletedFalseAndStatusNotIn(
            IssuePriority priority, Instant createdBefore, List<IssueStatus> excludedStatuses);
}
