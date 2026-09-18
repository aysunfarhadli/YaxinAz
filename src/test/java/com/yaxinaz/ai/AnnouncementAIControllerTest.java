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
 * MockCommunityAIService here - never calls the real Claude API (spec section 132).
 */
class AnnouncementAIControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void approvedMemberGetsAnnouncementDraft() throws Exception {
        User admin = createUser("ann-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        String token = login(admin.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/posts/draft")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawNotes":"water will be shut off tomorrow 10am to 2pm for maintenance",
                                 "postType":"ALERT"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.provider").value("MOCK"));
    }

    @Test
    void nonMemberCannotDraftAnnouncementForCommunity() throws Exception {
        User admin = createUser("ann-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User outsider = createUser("ann-outsider2@example.com", Role.RESIDENT);
        String token = login(outsider.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/posts/draft")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawNotes":"pool closed for cleaning"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void blankNotesAreRejected() throws Exception {
        User admin = createUser("ann-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        String token = login(admin.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/posts/draft")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rawNotes\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private User createUser(String email, Role role) {
        User user = User.builder()
                .firstName("Ann").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true)
                .build();
        return userRepository.save(user);
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Announcement Test Community " + admin.getId())
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
