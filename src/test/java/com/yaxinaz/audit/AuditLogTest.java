package com.yaxinaz.audit;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditLogTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void loginAndMembershipApprovalWriteRealAuditRows() throws Exception {
        User admin = createUser("audit-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("audit-resident1@example.com", Role.RESIDENT);
        String adminToken = login(admin);
        String residentToken = login(resident);

        List<AuditLog> loginLogs = auditLogRepository.findAll().stream()
                .filter(l -> l.getActionType() == AuditActionType.LOGIN_SUCCESS)
                .toList();
        assertTrue(loginLogs.size() >= 2, "expected at least 2 LOGIN_SUCCESS rows, found " + loginLogs.size());

        String joinResponse = mockMvc.perform(post("/api/communities/" + community.getId() + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andReturn().getResponse().getContentAsString();
        long membershipId = extractLong(joinResponse, "\"membershipId\":");

        mockMvc.perform(patch("/api/communities/" + community.getId() + "/members/" + membershipId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk());

        boolean hasMembershipApproved = auditLogRepository.findAll().stream()
                .anyMatch(l -> l.getActionType() == AuditActionType.MEMBERSHIP_APPROVED
                        && l.getResourceId().equals(membershipId));
        assertTrue(hasMembershipApproved);
    }

    @Test
    void issueStatusChangeWritesAuditRow() throws Exception {
        User admin = createUser("audit-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("audit-resident2@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String adminToken = login(admin);
        String residentToken = login(resident);

        String issueResponse = mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Test","description":"Test issue","category":"OTHER"}"""))
                .andReturn().getResponse().getContentAsString();
        long issueId = extractLong(issueResponse, "\"id\":");

        mockMvc.perform(patch("/api/issues/" + issueId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACKNOWLEDGED\"}"))
                .andExpect(status().isOk());

        boolean hasStatusChange = auditLogRepository.findAll().stream()
                .anyMatch(l -> l.getActionType() == AuditActionType.ISSUE_STATUS_CHANGED
                        && l.getResourceId().equals(issueId));
        assertTrue(hasStatusChange);
    }

    @Test
    void onlyPlatformAdminCanViewAuditLog() throws Exception {
        User resident = createUser("audit-resident3@example.com", Role.RESIDENT);
        User platformAdmin = createUser("audit-platform-admin3@example.com", Role.PLATFORM_ADMIN);
        String residentToken = login(resident);
        String adminToken = login(platformAdmin);

        mockMvc.perform(get("/api/admin/audit-log").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/audit-log").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Audit").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Audit Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
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
