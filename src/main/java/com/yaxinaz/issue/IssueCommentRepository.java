package com.yaxinaz.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

    Page<IssueComment> findAllByIssueIdOrderByCreatedAtAsc(Long issueId, Pageable pageable);

    long countByIssueId(Long issueId);
}
