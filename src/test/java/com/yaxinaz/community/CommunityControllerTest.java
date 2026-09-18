package com.yaxinaz.community;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommunityControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void residentCanJoinCommunityAndAdminCanApprove() throws Exception {
        String adminToken = createUserAndLogin("admin@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(adminToken, "Green Park Residence");
        String residentToken = createUserAndLogin("resident@example.com", Role.RESIDENT);

        String joinResponse = mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        long membershipId = extractLong(joinResponse, "\"membershipId\":");

        mockMvc.perform(patch("/api/communities/" + communityId + "/members/" + membershipId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void duplicateJoinRequestIsRejected() throws Exception {
        String adminToken = createUserAndLogin("admin2@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(adminToken, "Sunrise Complex");
        String residentToken = createUserAndLogin("resident2@example.com", Role.RESIDENT);

        mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanRejectMembership() throws Exception {
        String adminToken = createUserAndLogin("admin3@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(adminToken, "Lakeside Towers");
        String residentToken = createUserAndLogin("resident3@example.com", Role.RESIDENT);

        String joinResponse = mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andReturn().getResponse().getContentAsString();
        long membershipId = extractLong(joinResponse, "\"membershipId\":");

        mockMvc.perform(patch("/api/communities/" + communityId + "/members/" + membershipId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void adminCanBlockMembership() throws Exception {
        String adminToken = createUserAndLogin("admin4@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(adminToken, "Riverside Homes");
        String residentToken = createUserAndLogin("resident4@example.com", Role.RESIDENT);

        String joinResponse = mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andReturn().getResponse().getContentAsString();
        long membershipId = extractLong(joinResponse, "\"membershipId\":");

        mockMvc.perform(patch("/api/communities/" + communityId + "/members/" + membershipId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void nonAdminCannotReviewMembership() throws Exception {
        String adminToken = createUserAndLogin("admin5@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(adminToken, "Old Town Blocks");
        String residentToken = createUserAndLogin("resident5@example.com", Role.RESIDENT);
        String otherResidentToken = createUserAndLogin("resident5b@example.com", Role.RESIDENT);

        String joinResponse = mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andReturn().getResponse().getContentAsString();
        long membershipId = extractLong(joinResponse, "\"membershipId\":");

        mockMvc.perform(patch("/api/communities/" + communityId + "/members/" + membershipId)
                        .header("Authorization", "Bearer " + otherResidentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void communityAdminCannotReviewMembershipOfCommunityTheyDoNotOwn() throws Exception {
        String owningAdminToken = createUserAndLogin("owner-admin@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(owningAdminToken, "Central Court");
        String otherAdminToken = createUserAndLogin("other-admin@example.com", Role.COMMUNITY_ADMIN);
        String residentToken = createUserAndLogin("resident6@example.com", Role.RESIDENT);

        String joinResponse = mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andReturn().getResponse().getContentAsString();
        long membershipId = extractLong(joinResponse, "\"membershipId\":");

        mockMvc.perform(patch("/api/communities/" + communityId + "/members/" + membershipId)
                        .header("Authorization", "Bearer " + otherAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void pendingMemberListIsHiddenFromRegularResidents() throws Exception {
        String adminToken = createUserAndLogin("admin6@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(adminToken, "Park View");
        String residentToken = createUserAndLogin("resident7@example.com", Role.RESIDENT);

        mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/communities/" + communityId + "/members?status=PENDING")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/communities/" + communityId + "/members?status=PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void myCommunitiesReturnsOnlyApprovedMemberships() throws Exception {
        String adminToken = createUserAndLogin("admin8@example.com", Role.COMMUNITY_ADMIN);
        Long communityId = createCommunity(adminToken, "Selected Community");
        String residentToken = createUserAndLogin("resident8@example.com", Role.RESIDENT);

        mockMvc.perform(get("/api/communities/mine").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/communities/" + communityId + "/join")
                        .header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/communities/mine").header("Authorization", "Bearer " + residentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/communities/mine").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Selected Community"));
    }

    private String createUserAndLogin(String email, Role role) throws Exception {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role)
                .preferredLanguage("EN")
                .enabled(true)
                .build();
        userRepository.save(user);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return response.split("\"token\":\"")[1].split("\"")[0];
    }

    private Long createCommunity(String adminToken, String name) throws Exception {
        String response = mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"Demo community","type":"APARTMENT_BUILDING","city":"Baku"}"""
                                .formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return extractLong(response, "\"id\":");
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
