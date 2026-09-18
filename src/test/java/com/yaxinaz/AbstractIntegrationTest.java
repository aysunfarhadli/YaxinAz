package com.yaxinaz;

import com.yaxinaz.ai.AIUsageLogRepository;
import com.yaxinaz.community.CommunityMembershipRepository;
import com.yaxinaz.community.CommunityRepository;
import com.yaxinaz.event.CommunityEventRepository;
import com.yaxinaz.event.EventAttendanceRepository;
import com.yaxinaz.feed.PostCommentRepository;
import com.yaxinaz.feed.PostLikeRepository;
import com.yaxinaz.feed.PostRepository;
import com.yaxinaz.issue.IssueActivityRepository;
import com.yaxinaz.issue.IssueCommentRepository;
import com.yaxinaz.issue.IssueRepository;
import com.yaxinaz.issue.IssueSupportRepository;
import com.yaxinaz.audit.AuditLogRepository;
import com.yaxinaz.lostfound.LostFoundRepository;
import com.yaxinaz.moderation.ContentReportRepository;
import com.yaxinaz.notification.NotificationRepository;
import com.yaxinaz.poll.PollOptionRepository;
import com.yaxinaz.poll.PollRepository;
import com.yaxinaz.poll.PollVoteRepository;
import com.yaxinaz.provider.ProviderProfileRepository;
import com.yaxinaz.review.ReviewRepository;
import com.yaxinaz.servicerequest.ServiceRequestRepository;
import com.yaxinaz.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring caches the application context across test classes that share identical configuration
 * (same {@code @SpringBootTest} + {@code @ActiveProfiles}), which means every integration test
 * class in this project runs against the SAME in-memory H2 database within one test run - not an
 * isolated one per class. A test class that only deletes the tables it directly touches can fail
 * with a foreign-key violation when an earlier class in the same run left rows in a table this
 * class doesn't know about (e.g. an Issue created by IssueControllerTest still referencing a User
 * that AuthControllerTest's cleanup then tries to delete).
 * <p>
 * Every integration test extends this class and gets a full, dependency-ordered wipe before each
 * test method, regardless of which classes ran before it. Add a `deleteAll()` line here for any new
 * FK-bearing table a future phase introduces.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired protected UserRepository userRepository;
    @Autowired protected CommunityRepository communityRepository;
    @Autowired protected CommunityMembershipRepository membershipRepository;
    @Autowired protected IssueRepository issueRepository;
    @Autowired protected IssueSupportRepository issueSupportRepository;
    @Autowired protected IssueCommentRepository issueCommentRepository;
    @Autowired protected IssueActivityRepository issueActivityRepository;
    @Autowired protected AIUsageLogRepository aiUsageLogRepository;
    @Autowired protected PostLikeRepository postLikeRepository;
    @Autowired protected PostCommentRepository postCommentRepository;
    @Autowired protected PostRepository postRepository;
    @Autowired protected PollVoteRepository pollVoteRepository;
    @Autowired protected PollOptionRepository pollOptionRepository;
    @Autowired protected PollRepository pollRepository;
    @Autowired protected EventAttendanceRepository eventAttendanceRepository;
    @Autowired protected CommunityEventRepository communityEventRepository;
    @Autowired protected LostFoundRepository lostFoundRepository;
    @Autowired protected ReviewRepository reviewRepository;
    @Autowired protected ServiceRequestRepository serviceRequestRepository;
    @Autowired protected ProviderProfileRepository providerProfileRepository;
    @Autowired protected NotificationRepository notificationRepository;
    @Autowired protected ContentReportRepository contentReportRepository;
    @Autowired protected AuditLogRepository auditLogRepository;

    @BeforeEach
    void resetDatabase() {
        aiUsageLogRepository.deleteAll();
        notificationRepository.deleteAll();
        contentReportRepository.deleteAll();
        auditLogRepository.deleteAll();
        reviewRepository.deleteAll();
        serviceRequestRepository.deleteAll();
        providerProfileRepository.deleteAll();
        issueSupportRepository.deleteAll();
        issueCommentRepository.deleteAll();
        issueActivityRepository.deleteAll();
        issueRepository.deleteAll();
        postLikeRepository.deleteAll();
        postCommentRepository.deleteAll();
        postRepository.deleteAll();
        pollVoteRepository.deleteAll();
        pollOptionRepository.deleteAll();
        pollRepository.deleteAll();
        eventAttendanceRepository.deleteAll();
        communityEventRepository.deleteAll();
        lostFoundRepository.deleteAll();
        membershipRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();
    }
}
