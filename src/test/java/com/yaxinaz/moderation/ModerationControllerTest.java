package com.yaxinaz.moderation;

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

class ModerationControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void residentCanReportContentAndAdminCanResolveIt() throws Exception {
        User resident = createUser("mod-resident1@example.com", Role.RESIDENT);
        User platformAdmin = createUser("mod-platform-admin1@example.com", Role.PLATFORM_ADMIN);
        String residentToken = login(resident);
        String adminToken = login(platformAdmin);

        String response = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"POST","contentId":42,"reason":"SPAM","description":"Looks like spam"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long reportId = extractLong(response, "\"id\":");

        mockMvc.perform(get("/api/admin/reports").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(patch("/api/admin/reports/" + reportId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTION_TAKEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTION_TAKEN"));
    }

    @Test
    void nonAdminCannotViewModerationQueue() throws Exception {
        User resident = createUser("mod-resident2@example.com", Role.RESIDENT);
        String token = login(resident);

        mockMvc.perform(get("/api/admin/reports").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotResolveAReport() throws Exception {
        User resident = createUser("mod-resident3@example.com", Role.RESIDENT);
        User otherResident = createUser("mod-resident4@example.com", Role.RESIDENT);
        String residentToken = login(resident);
        String otherToken = login(otherResident);

        String response = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contentType":"POST","contentId":99,"reason":"SPAM"}"""))
                .andReturn().getResponse().getContentAsString();
        long reportId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/admin/reports/" + reportId + "/status")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTION_TAKEN\"}"))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Mod").lastName("Test").email(email)
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
