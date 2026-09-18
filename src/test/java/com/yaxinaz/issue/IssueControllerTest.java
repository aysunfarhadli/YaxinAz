package com.yaxinaz.issue;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IssueControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void approvedResidentCanCreateIssueAndAdminCanTransitionStatus() throws Exception {
        User admin = createUser("issue-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("issue-resident1@example.com", Role.RESIDENT);
        approveMembership(resident, community);

        String residentToken = login(resident.getEmail());
        String adminToken = login(admin.getEmail());

        String createResponse = mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Elevator broken","description":"Elevator in building B stopped working",
                                 "category":"ELEVATOR","buildingOrLocation":"Building B"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andReturn().getResponse().getContentAsString();
        long issueId = extractLong(createResponse, "\"id\":");

        mockMvc.perform(patch("/api/issues/" + issueId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACKNOWLEDGED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));

        mockMvc.perform(get("/api/issues/" + issueId + "/timeline")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CREATED"))
                .andExpect(jsonPath("$[1].type").value("STATUS_CHANGED"));
    }

    @Test
    void nonApprovedResidentCannotCreateIssue() throws Exception {
        User admin = createUser("issue-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("issue-resident2@example.com", Role.RESIDENT);
        String residentToken = login(resident.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Water leak","description":"Water leaking in the hallway","category":"WATER"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidStatusTransitionIsRejected() throws Exception {
        User admin = createUser("issue-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("issue-resident3@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());
        String adminToken = login(admin.getEmail());

        long issueId = createIssue(community, residentToken);

        mockMvc.perform(patch("/api/issues/" + issueId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void residentCannotChangeIssueStatus() throws Exception {
        User admin = createUser("issue-admin4@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("issue-resident4@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());

        long issueId = createIssue(community, residentToken);

        mockMvc.perform(patch("/api/issues/" + issueId + "/status")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACKNOWLEDGED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportingIssueTwiceIsRejected() throws Exception {
        User admin = createUser("issue-admin5@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("issue-resident5@example.com", Role.RESIDENT);
        User supporter = createUser("issue-supporter5@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        approveMembership(supporter, community);
        String residentToken = login(resident.getEmail());
        String supporterToken = login(supporter.getEmail());

        long issueId = createIssue(community, residentToken);

        mockMvc.perform(post("/api/issues/" + issueId + "/support")
                        .header("Authorization", "Bearer " + supporterToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/issues/" + issueId + "/support")
                        .header("Authorization", "Bearer " + supporterToken))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/issues/" + issueId)
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supportCount").value(1));
    }

    @Test
    void residentFromAnotherCommunityCannotViewIssue() throws Exception {
        User admin = createUser("issue-admin6@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("issue-resident6@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());
        long issueId = createIssue(community, residentToken);

        User outsiderAdmin = createUser("issue-admin6b@example.com", Role.COMMUNITY_ADMIN);
        Community otherCommunity = createCommunity(outsiderAdmin);
        User outsider = createUser("issue-outsider6@example.com", Role.RESIDENT);
        approveMembership(outsider, otherCommunity);
        String outsiderToken = login(outsider.getEmail());

        mockMvc.perform(get("/api/issues/" + issueId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void excessiveIssueCreationRequestsAreRateLimited() throws Exception {
        User admin = createUser("issue-admin7@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("issue-resident7@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());

        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                            .header("Authorization", "Bearer " + residentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"Test issue","description":"Something is wrong","category":"OTHER"}"""))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Test issue","description":"Something is wrong","category":"OTHER"}"""))
                .andExpect(status().isTooManyRequests());
    }

    private long createIssue(Community community, String residentToken) throws Exception {
        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Test issue","description":"Something is wrong","category":"OTHER"}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractLong(response, "\"id\":");
    }

    private User createUser(String email, Role role) {
        User user = User.builder()
                .firstName("Test").lastName("User").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true)
                .build();
        return userRepository.save(user);
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Community of " + admin.getId())
                .description("Demo").type(CommunityType.APARTMENT_BUILDING)
                .city("Baku").createdBy(admin.getId())
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

    private long extractLong(String json, String field) {
        String after = json.substring(json.indexOf(field) + field.length());
        StringBuilder digits = new StringBuilder();
        for (char c : after.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (!digits.isEmpty()) {
                break;
            }
        }
        return Long.parseLong(digits.toString());
    }
}
