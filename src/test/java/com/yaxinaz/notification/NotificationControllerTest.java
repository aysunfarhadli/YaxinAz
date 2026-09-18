package com.yaxinaz.notification;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void issueStatusChangeCreatesNotificationForReporter() throws Exception {
        User admin = createUser("notif-admin1@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("notif-resident1@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String adminToken = login(admin.getEmail());
        String residentToken = login(resident.getEmail());

        String response = mockMvc.perform(post("/api/communities/" + community.getId() + "/issues")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Broken light","description":"Hallway light is out","category":"LIGHTING"}"""))
                .andReturn().getResponse().getContentAsString();
        long issueId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/issues/" + issueId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACKNOWLEDGED\"}"))
                .andExpect(status().isOk());

        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(
                resident.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertEquals(1, notifications.size());
        assertEquals(NotificationType.ISSUE_STATUS_UPDATED, notifications.get(0).getType());
        assertTrue(notifications.get(0).getMessage().contains("ACKNOWLEDGED"));
    }

    @Test
    void membershipApprovalCreatesNotification() throws Exception {
        User admin = createUser("notif-admin2@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("notif-resident2@example.com", Role.RESIDENT);
        String adminToken = login(admin.getEmail());
        String residentToken = login(resident.getEmail());

        String joinResponse = mockMvc.perform(post("/api/communities/" + community.getId() + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andReturn().getResponse().getContentAsString();
        long membershipId = extractLong(joinResponse, "\"membershipId\":");

        mockMvc.perform(patch("/api/communities/" + community.getId() + "/members/" + membershipId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("MEMBERSHIP_APPROVED"));

        mockMvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));
    }

    @Test
    void residentCanMarkNotificationReadAndReadAll() throws Exception {
        User admin = createUser("notif-admin3@example.com", Role.COMMUNITY_ADMIN);
        Community community = createCommunity(admin);
        User resident = createUser("notif-resident3@example.com", Role.RESIDENT);
        approveMembership(resident, community);
        String residentToken = login(resident.getEmail());

        notificationRepository.save(Notification.builder()
                .userId(resident.getId()).type(NotificationType.NEW_POLL)
                .title("New poll").message("A new poll was created").read(false).build());
        notificationRepository.save(Notification.builder()
                .userId(resident.getId()).type(NotificationType.EVENT_REMINDER)
                .title("Event reminder").message("Event starts soon").read(false).build());

        mockMvc.perform(patch("/api/notifications/read-all").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notifications/unread-count").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    void userCannotMarkAnotherUsersNotificationAsRead() throws Exception {
        User owner = createUser("notif-owner4@example.com", Role.RESIDENT);
        User intruder = createUser("notif-intruder4@example.com", Role.RESIDENT);
        String intruderToken = login(intruder.getEmail());

        Notification notification = notificationRepository.save(Notification.builder()
                .userId(owner.getId()).type(NotificationType.NEW_POLL)
                .title("New poll").message("msg").read(false).build());

        mockMvc.perform(patch("/api/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Notif").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private Community createCommunity(User admin) {
        Community community = communityRepository.save(Community.builder()
                .name("Notif Community " + admin.getId()).type(CommunityType.APARTMENT_BUILDING)
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
