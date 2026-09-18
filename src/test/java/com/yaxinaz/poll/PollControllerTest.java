package com.yaxinaz.poll;

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

class PollControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void residentCanVoteAndSeesPercentages() throws Exception {
        User admin = createUser("poll-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("poll-resident1@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String adminToken = login(admin.getEmail());
        String residentToken = login(resident.getEmail());

        String pollResponse = mockMvc.perform(post("/api/communities/" + community.getId() + "/polls")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"Should EV charging stations be installed near Building A?",
                                 "options":["Yes","No","Need more information"]}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long pollId = extractLong(pollResponse, "\"id\":");
        long firstOptionId = extractLong(pollResponse, "\"id\":", pollResponse.indexOf("\"options\""));

        mockMvc.perform(post("/api/communities/" + community.getId() + "/polls/" + pollId + "/vote")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionId\":" + firstOptionId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1))
                .andExpect(jsonPath("$.votedByCurrentUser").value(true));
    }

    @Test
    void duplicateVoteIsRejected() throws Exception {
        User admin = createUser("poll-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("poll-resident2@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String adminToken = login(admin.getEmail());
        String residentToken = login(resident.getEmail());

        String pollResponse = mockMvc.perform(post("/api/communities/" + community.getId() + "/polls")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"Test poll?","options":["Yes","No"]}"""))
                .andReturn().getResponse().getContentAsString();
        long pollId = extractLong(pollResponse, "\"id\":");
        long optionId = extractLong(pollResponse, "\"id\":", pollResponse.indexOf("\"options\""));

        mockMvc.perform(post("/api/communities/" + community.getId() + "/polls/" + pollId + "/vote")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionId\":" + optionId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/polls/" + pollId + "/vote")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionId\":" + optionId + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void votingOnExpiredPollIsRejected() throws Exception {
        User admin = createUser("poll-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("poll-resident3@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());

        Poll poll = pollRepository.save(Poll.builder()
                .community(community).question("Expired poll?").createdBy(admin.getId())
                .expiresAt(Instant.now().minusSeconds(3600)).active(true).build());
        PollOption option = pollOptionRepository.save(PollOption.builder().poll(poll).optionText("Yes").build());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/polls/" + poll.getId() + "/vote")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"optionId\":" + option.getId() + "}"))
                .andExpect(status().isBadRequest());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Poll").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Poll Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
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
        return extractLong(json, field, 0);
    }

    private long extractLong(String json, String field, int fromIndex) {
        String after = json.substring(json.indexOf(field, fromIndex) + field.length());
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
