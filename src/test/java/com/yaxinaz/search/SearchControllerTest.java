package com.yaxinaz.search;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void searchFindsMatchingIssueByKeyword() throws Exception {
        User admin = createUser("search-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        String token = login(admin);

        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Elevator broken in Building B","description":"The elevator has stopped working","category":"ELEVATOR"}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/search").param("q", "elevator").param("communityId", String.valueOf(community.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issues.length()").value(1))
                .andExpect(jsonPath("$.issues[0].title").value("Elevator broken in Building B"));

        mockMvc.perform(get("/api/search").param("q", "nonexistentxyz").param("communityId", String.valueOf(community.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issues.length()").value(0));
    }

    @Test
    void smartSearchReturnsAiProviderAndUsesInterpretedKeywords() throws Exception {
        User admin = createUser("search-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        String token = login(admin);

        mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Elevator broken in Building B","description":"The elevator has stopped working","category":"ELEVATOR"}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/search")
                        .param("q", "find elevator").param("communityId", String.valueOf(community.getId()))
                        .param("useAi", "true").param("lang", "EN")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiProvider").value("MOCK"))
                .andExpect(jsonPath("$.issues[0].title").value("Elevator broken in Building B"));
    }

    @Test
    void excessiveSmartSearchRequestsAreRateLimited() throws Exception {
        User admin = createUser("search-admin4@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        String token = login(admin);

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/search")
                            .param("q", "elevator").param("communityId", String.valueOf(community.getId()))
                            .param("useAi", "true")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/search")
                        .param("q", "elevator").param("communityId", String.valueOf(community.getId()))
                        .param("useAi", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void nonMemberCannotSearchWithinCommunity() throws Exception {
        User admin = createUser("search-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User outsider = createUser("search-outsider2@example.com", Role.RESIDENT);
        String token = login(outsider);

        mockMvc.perform(get("/api/search").param("q", "test").param("communityId", String.valueOf(community.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Search").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Search Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
                .city("Baku").createdBy(admin.getId()).build());
        membershipRepository.save(CommunityMembership.builder()
                .user(admin).community(community).status(MembershipStatus.APPROVED)
                .joinedAt(Instant.now()).build());
        return community;
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
