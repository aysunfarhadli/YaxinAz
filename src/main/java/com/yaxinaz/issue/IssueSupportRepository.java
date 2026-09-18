package com.yaxinaz.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueSupportRepository extends JpaRepository<IssueSupport, Long> {

    boolean existsByIssueIdAndUserId(Long issueId, Long userId);

    Optional<IssueSupport> findByIssueIdAndUserId(Long issueId, Long userId);

    long countByIssueId(Long issueId);

    @Query("select s.issue.id as issueId, count(s) as total from IssueSupport s "
            + "where s.issue.id in :issueIds group by s.issue.id")
    List<IssueSupportCount> countByIssueIdIn(@Param("issueIds") List<Long> issueIds);

    interface IssueSupportCount {
        Long getIssueId();
        Long getTotal();
    }
}
