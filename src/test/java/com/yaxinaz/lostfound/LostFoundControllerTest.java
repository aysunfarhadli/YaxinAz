package com.yaxinaz.lostfound;

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

class LostFoundControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void residentCanReportAndOwnerCanUpdateStatus() throws Exception {
        User admin = createUser("lf-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("lf-resident1@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/lost-found")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"LOST","title":"Lost Cat","description":"Orange tabby, answers to Milo","location":"Near Building A"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        long itemId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/communities/" + community.getId() + "/lost-found/" + itemId + "/status")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RETURNED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    @Test
    void otherResidentCannotUpdateSomeoneElsesItemStatus() throws Exception {
        User admin = createUser("lf-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("lf-resident2@example.com", Role.RESIDENT);
        User other = createUser("lf-other2@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        approveMembership(other, community);
        String residentToken = login(resident.getEmail());
        String otherToken = login(other.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/lost-found")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"FOUND","title":"Found Wallet","location":"Lobby"}"""))
                .andReturn().getResponse().getContentAsString();
        long itemId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/communities/" + community.getId() + "/lost-found/" + itemId + "/status")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLAIMED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listCanFilterByType() throws Exception {
        User admin = createUser("lf-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("lf-resident3@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String token = login(resident.getEmail());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/lost-found")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"LOST","title":"Lost AirPods"}"""))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/communities/" + community.getId() + "/lost-found")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"FOUND","title":"Found Keys"}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/communities/" + community.getId() + "/lost-found?type=LOST")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Lost AirPods"));
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("LF").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("LF Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
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
