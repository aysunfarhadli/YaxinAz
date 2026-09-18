package com.yaxinaz.ai;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityMembership;
import com.yaxinaz.community.CommunityType;
import com.yaxinaz.community.MembershipStatus;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test profile has yaxinaz.ai.enabled=false, so CommunityAIServiceRouter always short-circuits to
 * MockCommunityAIService - these tests never call the real Claude API (spec section 132).
 */
class IssueAIControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void approvedMemberGetsMockAnalysisWhenAiDisabled() throws Exception {
        User admin = createUser("ai-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("ai-resident1@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String token = login(resident.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawText":"The elevator in Building B has stopped working again and elderly residents cannot use the stairs.",
                                 "buildingOrLocation":"Building B"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.category").value("ELEVATOR"))
                .andExpect(jsonPath("$.analysis.provider").value("MOCK"))
                .andExpect(jsonPath("$.possibleDuplicates").isArray());

        long usageCount = aiUsageLogRepository.count();
        org.junit.jupiter.api.Assertions.assertEquals(1, usageCount);
    }

    @Test
    void analyzeSurfacesExistingSimilarOpenIssueAsDuplicate() throws Exception {
        User admin = createUser("ai-admin4@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User firstReporter = createUser("ai-resident4a@example.com", Role.RESIDENT);
        User secondReporter = createUser("ai-resident4b@example.com", Role.RESIDENT);
        approveMembership(firstReporter, community);
        approveMembership(secondReporter, community);

        String firstToken = login(firstReporter.getEmail());
        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + firstToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Elevator broken","description":"The elevator in Building B has stopped working and is unsafe.",
                                 "category":"ELEVATOR","buildingOrLocation":"Building B"}"""))
                .andExpect(status().isCreated());

        String secondToken = login(secondReporter.getEmail());
        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues/analyze")
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawText":"The elevator in Building B has stopped working again and elderly residents cannot use the stairs.",
                                 "buildingOrLocation":"Building B"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possibleDuplicates.length()").value(1))
                .andExpect(jsonPath("$.possibleDuplicates[0].title").value("Elevator broken"));
    }

    @Test
    void nonMemberCannotAnalyzeIssueForCommunity() throws Exception {
        User admin = createUser("ai-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User outsider = createUser("ai-outsider2@example.com", Role.RESIDENT);
        String token = login(outsider.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawText":"Something is broken in the hallway."}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void excessiveAnalyzeRequestsAreRateLimited() throws Exception {
        User admin = createUser("ai-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("ai-resident3@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String token = login(resident.getEmail());

        String body = """
                {"rawText":"There is a noise complaint from the neighbors upstairs."}""";

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/communities/" + community.getId() + "/issues/analyze")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues/analyze")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests());
    }

    private User createUser(String email, Role role) {
        User user = User.builder()
                .firstName("AI").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true)
                .build();
        return userRepository.save(user);
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("AI Test Community " + admin.getId())
                .type(CommunityType.APARTMENT_BUILDING).city("Baku").createdBy(admin.getId())
                .build());
        approveMembership(admin, community);
        return community;
    }

    private void approveMembership(User user, Community community) {
        membershipRepository.save(CommunityMembership.builder()
                .user(user).community(community).status(MembershipStatus.APPROVED)
                .joinedAt(Instant.now()).build());
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return response.split("\"token\":\"")[1].split("\"")[0];
    }
}
