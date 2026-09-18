package com.yaxinaz.health;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SystemHealthControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void platformAdminSeesSystemHealth() throws Exception {
        String token = login(createUser("health-admin1@example.com", Role.PLATFORM_ADMIN));

        mockMvc.perform(get("/api/admin/system-health").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationStatus").value("ONLINE"))
                .andExpect(jsonPath("$.databaseStatus").value("ONLINE"))
                .andExpect(jsonPath("$.activeProfile").value("test"));
    }

    @Test
    void residentCannotViewSystemHealth() throws Exception {
        String token = login(createUser("health-resident1@example.com", Role.RESIDENT));

        mockMvc.perform(get("/api/admin/system-health").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void operationsCenterReflectsSeededCriticalAndStaleIssues() throws Exception {
        User admin = createUser("health-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = communityRepository.save(Community.builder()
                .name("Health Community").type(CommunityType.APARTMENT_BUILDING)
                .city("Baku").createdBy(admin.getId()).build());

        issueRepository.save(Issue.builder()
                .community(community).createdBy(admin.getId())
                .title("Critical issue").description("desc")
                .category(IssueCategory.ELEVATOR).priority(IssuePriority.CRITICAL)
                .status(IssueStatus.OPEN).build());
        issueRepository.save(Issue.builder()
                .community(community).createdBy(admin.getId())
                .title("Stale issue").description("desc")
                .category(IssueCategory.WATER).priority(IssuePriority.HIGH)
                .status(IssueStatus.OPEN).stale(true).build());

        String platformAdminToken = login(createUser("health-platform-admin2@example.com", Role.PLATFORM_ADMIN));

        mockMvc.perform(get("/api/admin/operations-center").header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalIssuesCount").value(1))
                .andExpect(jsonPath("$.staleIssuesCount").value(1))
                .andExpect(jsonPath("$.systemHealth.applicationStatus").value("ONLINE"));
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Health").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
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
