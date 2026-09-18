package com.yaxinaz.issue;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityType;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spec sections 43-44: stale/escalation flags are set by plain Java rules, no AI. Timestamps are
 * backdated directly via JDBC because JPA auditing (@LastModifiedDate/@CreatedDate) would otherwise
 * overwrite any value we set through the entity on save.
 */
class IssueMaintenanceSchedulerTest extends AbstractIntegrationTest {

    @Autowired private IssueMaintenanceScheduler scheduler;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Community community;

    @BeforeEach
    void setUp() {
        User admin = userRepository.save(User.builder()
                .firstName("Sched").lastName("Admin").email("sched-admin@example.com")
                .password("x").role(Role.COMMUNITY_ADMIN).preferredLanguage("EN").enabled(true).build());
        community = communityRepository.save(Community.builder()
                .name("Scheduler Test Community").type(CommunityType.APARTMENT_BUILDING)
                .city("Baku").createdBy(admin.getId()).build());
    }

    @Test
    void staleHighPriorityOpenIssueIsMarkedStale() {
        Issue issue = saveIssue(IssuePriority.HIGH, IssueStatus.OPEN);
        backdate(issue.getId(), Instant.now().minusSeconds(100 * 3600L));

        scheduler.checkStaleIssues();

        Issue reloaded = issueRepository.findById(issue.getId()).orElseThrow();
        assertTrue(reloaded.isStale());
    }

    @Test
    void freshHighPriorityIssueIsNotMarkedStale() {
        Issue issue = saveIssue(IssuePriority.HIGH, IssueStatus.OPEN);

        scheduler.checkStaleIssues();

        Issue reloaded = issueRepository.findById(issue.getId()).orElseThrow();
        assertFalse(reloaded.isStale());
    }

    @Test
    void oldCriticalOpenIssueIsEscalated() {
        Issue issue = saveIssue(IssuePriority.CRITICAL, IssueStatus.OPEN);
        backdate(issue.getId(), Instant.now().minusSeconds(3600L));

        scheduler.checkEscalations();

        Issue reloaded = issueRepository.findById(issue.getId()).orElseThrow();
        assertTrue(reloaded.isEscalated());
    }

    @Test
    void recentCriticalOpenIssueIsNotEscalated() {
        Issue issue = saveIssue(IssuePriority.CRITICAL, IssueStatus.OPEN);

        scheduler.checkEscalations();

        Issue reloaded = issueRepository.findById(issue.getId()).orElseThrow();
        assertFalse(reloaded.isEscalated());
    }

    private Issue saveIssue(IssuePriority priority, IssueStatus status) {
        return issueRepository.save(Issue.builder()
                .community(community).createdBy(community.getCreatedBy())
                .title("Scheduler test issue").description("desc")
                .category(IssueCategory.OTHER).priority(priority).status(status)
                .build());
    }

    private void backdate(Long issueId, Instant instant) {
        jdbcTemplate.update("UPDATE issues SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(instant), Timestamp.from(instant), issueId);
    }
}
