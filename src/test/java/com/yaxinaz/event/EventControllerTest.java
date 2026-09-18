package com.yaxinaz.event;

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

class EventControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void residentCanChangeAttendanceStatus() throws Exception {
        User admin = createUser("event-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("event-resident1@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String adminToken = login(admin.getEmail());
        String residentToken = login(resident.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Community Meeting","description":"Monthly meeting",
                                 "startTime":"2027-01-10T18:00:00Z","endTime":"2027-01-10T19:00:00Z"}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long eventId = extractLong(response, "\"id\":");

        mockMvc.perform(post("/api/communities/" + community.getId() + "/events/" + eventId + "/attendance")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"GOING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goingCount").value(1))
                .andExpect(jsonPath("$.myStatus").value("GOING"));

        mockMvc.perform(post("/api/communities/" + community.getId() + "/events/" + eventId + "/attendance")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MAYBE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goingCount").value(0))
                .andExpect(jsonPath("$.maybeCount").value(1))
                .andExpect(jsonPath("$.myStatus").value("MAYBE"));
    }

    @Test
    void attendanceRejectedOnceCapacityReached() throws Exception {
        User admin = createUser("event-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident1 = createUser("event-resident2a@example.com", Role.RESIDENT);
        User resident2 = createUser("event-resident2b@example.com", Role.RESIDENT);
        approveMembership(resident1, community);
        approveMembership(resident2, community);
        String adminToken = login(admin.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Kids Activity Day","startTime":"2027-02-01T10:00:00Z",
                                 "endTime":"2027-02-01T12:00:00Z","capacity":1}"""))
                .andReturn().getResponse().getContentAsString();
        long eventId = extractLong(response, "\"id\":");

        mockMvc.perform(post("/api/communities/" + community.getId() + "/events/" + eventId + "/attendance")
                        .header("Authorization", "Bearer " + login(resident1.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"GOING\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/communities/" + community.getId() + "/events/" + eventId + "/attendance")
                        .header("Authorization", "Bearer " + login(resident2.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"GOING\"}"))
                .andExpect(status().isConflict());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Event").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Event Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
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
