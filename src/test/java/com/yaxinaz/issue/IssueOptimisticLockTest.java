package com.yaxinaz.issue;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityType;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies spec section 68: concurrent modification of an Issue must be rejected via @Version,
 * not silently overwritten. Each repository call here runs in its own transaction/persistence
 * context (no @Transactional on this test class), so "copy1" and "copy2" are genuinely independent
 * managed instances - the same shape a second concurrent HTTP request would produce.
 */
class IssueOptimisticLockTest extends AbstractIntegrationTest {

    private Long issueId;

    @BeforeEach
    void setUp() {
        User admin = userRepository.save(User.builder()
                .firstName("Lock").lastName("Admin").email("lock-admin@example.com")
                .password("x").role(Role.COMMUNITY_ADMIN).preferredLanguage("EN").enabled(true).build());
        Community community = communityRepository.save(Community.builder()
                .name("Lock Test Community").type(CommunityType.APARTMENT_BUILDING)
                .city("Baku").createdBy(admin.getId()).build());
        Issue issue = issueRepository.save(Issue.builder()
                .community(community).createdBy(admin.getId())
                .title("Concurrency test issue").description("desc")
                .category(IssueCategory.OTHER).priority(IssuePriority.MEDIUM)
                .status(IssueStatus.OPEN).build());
        issueId = issue.getId();
    }

    @Test
    void concurrentUpdatesThrowOptimisticLockException() {
        Issue copy1 = issueRepository.findById(issueId).orElseThrow();
        Issue copy2 = issueRepository.findById(issueId).orElseThrow();

        copy1.setStatus(IssueStatus.ACKNOWLEDGED);
        issueRepository.saveAndFlush(copy1);

        copy2.setStatus(IssueStatus.REJECTED);
        assertThrows(OptimisticLockingFailureException.class, () -> issueRepository.saveAndFlush(copy2));
    }
}
