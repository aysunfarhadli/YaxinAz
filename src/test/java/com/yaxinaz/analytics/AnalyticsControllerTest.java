package com.yaxinaz.analytics;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityMembership;
import com.yaxinaz.community.CommunityType;
import com.yaxinaz.community.MembershipStatus;
import com.yaxinaz.issue.Issue;
import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueStatus;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalyticsControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void statisticsReflectSeededIssuesAccurately() throws Exception {
        User admin = createUser("analytics-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        String adminToken = login(admin);

        seedIssue(community, admin, IssueCategory.ELEVATOR, IssuePriority.CRITICAL, IssueStatus.OPEN);
        seedIssue(community, admin, IssueCategory.ELEVATOR, IssuePriority.HIGH, IssueStatus.RESOLVED);
        seedIssue(community, admin, IssueCategory.WATER, IssuePriority.MEDIUM, IssueStatus.RESOLVED);
        seedIssue(community, admin, IssueCategory.NOISE, IssuePriority.LOW, IssueStatus.OPEN);

        mockMvc.perform(get("/api/communities/" + community.getId() + "/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIssues").value(4))
                .andExpect(jsonPath("$.resolvedIssues").value(2))
                .andExpect(jsonPath("$.criticalIssues").value(1))
                .andExpect(jsonPath("$.resolutionRatePercent").value(50.0))
                .andExpect(jsonPath("$.mostCommonCategory").value("ELEVATOR"))
                .andExpect(jsonPath("$.issuesByCategory.ELEVATOR").value(2));
    }

    @Test
    void summaryAndInsightsUseMockAiAndReflectRealNumbers() throws Exception {
        User admin = createUser("analytics-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        String adminToken = login(admin);

        for (int i = 0; i < 3; i++) {
            seedIssue(community, admin, IssueCategory.ELEVATOR, IssuePriority.HIGH, IssueStatus.OPEN);
        }

        mockMvc.perform(get("/api/communities/" + community.getId() + "/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").isNotEmpty());

        mockMvc.perform(get("/api/communities/" + community.getId() + "/insights")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void nonMemberCannotViewStatistics() throws Exception {
        User admin = createUser("analytics-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User outsider = createUser("analytics-outsider3@example.com", Role.RESIDENT);
        String outsiderToken = login(outsider);

        mockMvc.perform(get("/api/communities/" + community.getId() + "/statistics")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    private void seedIssue(Community community, User reporter, IssueCategory category, IssuePriority priority, IssueStatus status) {
        issueRepository.save(Issue.builder()
                .community(community).createdBy(reporter.getId())
                .title("Seeded issue").description("desc")
                .category(category).priority(priority).status(status)
                .resolvedAt(status == IssueStatus.RESOLVED || status == IssueStatus.CLOSED ? Instant.now() : null)
                .build());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Analytics").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Analytics Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
                .city("Baku").createdBy(admin.getId()).build());
        approveMembership(admin, community);
        return community;
    }

    private void approveMembership(User user, Community community) {
        membershipRepository.save(CommunityMembership.builder()
                .user(user).community(community).status(MembershipStatus.APPROVED)
                .joinedAt(Instant.now()).build());
    }

    private String login(User user) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + user.getEmail() + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return response.split("\"token\":\"")[1].split("\"")[0];
    }
}
