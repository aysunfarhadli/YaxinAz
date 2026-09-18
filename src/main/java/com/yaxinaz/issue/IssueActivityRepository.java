package com.yaxinaz.issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueActivityRepository extends JpaRepository<IssueActivity, Long> {

    List<IssueActivity> findAllByIssueIdOrderByCreatedAtAsc(Long issueId);
}
