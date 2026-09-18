package com.yaxinaz.feed;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void residentCanCreateLikeAndCommentOnPost() throws Exception {
        User admin = createUser("post-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("post-resident1@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/posts")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postType":"GENERAL","content":"Does anyone know a good electrician?"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.likeCount").value(0))
                .andReturn().getResponse().getContentAsString();
        long postId = extractLong(response, "\"id\":");

        mockMvc.perform(post("/api/posts/" + postId + "/like").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Try Volt Electric, they are great.\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/posts/" + postId + "/like").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void onlyCommunityAdminCanPinPost() throws Exception {
        User admin = createUser("post-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("post-resident2@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String adminToken = login(admin.getEmail());
        String residentToken = login(resident.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/posts")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postType":"ANNOUNCEMENT","content":"Water will be shut off tomorrow."}"""))
                .andReturn().getResponse().getContentAsString();
        long postId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/posts/" + postId + "/pin?pinned=true").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/posts/" + postId + "/pin?pinned=true").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void nonMemberCannotCreatePost() throws Exception {
        User admin = createUser("post-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User outsider = createUser("post-outsider3@example.com", Role.RESIDENT);
        String token = login(outsider.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postType":"GENERAL","content":"Hello"}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorCanDeleteOwnPostAndItDisappears() throws Exception {
        User admin = createUser("post-admin4@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("post-resident4@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/posts")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postType":"GENERAL","content":"Post to be deleted by its own author."}"""))
                .andReturn().getResponse().getContentAsString();
        long postId = extractLong(response, "\"id\":");

        mockMvc.perform(delete("/api/posts/" + postId).header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId).header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void communityAdminCanDeleteSomeoneElsesPostButOtherResidentsCannot() throws Exception {
        User admin = createUser("post-admin5@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User author = createUser("post-author5@example.com", Role.RESIDENT);
        User otherResident = createUser("post-other5@example.com", Role.RESIDENT);
        approveMembership(author, community);
        approveMembership(otherResident, community);
        String authorToken = login(author.getEmail());
        String otherToken = login(otherResident.getEmail());
        String adminToken = login(admin.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/posts")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postType":"GENERAL","content":"Only the author or an admin may delete this."}"""))
                .andReturn().getResponse().getContentAsString();
        long postId = extractLong(response, "\"id\":");

        mockMvc.perform(delete("/api/posts/" + postId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/posts/" + postId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Post").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Post Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
                .city("Baku").createdBy(admin.getId()).build());
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
