package com.yaxinaz.provider;

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

class ProviderControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void serviceProviderCanCreateProfileAndAppearsInFilteredList() throws Exception {
        String token = login(createUser("prov1@example.com", Role.SERVICE_PROVIDER));

        mockMvc.perform(post("/api/providers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"FixPro Plumbing","bio":"Reliable plumbing","serviceArea":"Baku",
                                 "categories":["PLUMBING"]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verified").value(false));

        mockMvc.perform(get("/api/providers?category=PLUMBING")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/providers?category=ELECTRICAL")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void cannotCreateSecondProfileForSameUser() throws Exception {
        String token = login(createUser("prov2@example.com", Role.SERVICE_PROVIDER));
        String body = """
                {"businessName":"Volt Electric","categories":["ELECTRICAL"]}""";

        mockMvc.perform(post("/api/providers").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/providers").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyPlatformAdminCanVerifyProvider() throws Exception {
        User provider = createUser("prov3@example.com", Role.SERVICE_PROVIDER);
        String providerToken = login(provider);
        String response = mockMvc.perform(post("/api/providers")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"CleanHome","categories":["CLEANING"]}"""))
                .andReturn().getResponse().getContentAsString();
        long providerId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/providers/" + providerId + "/verify")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verified\":true}"))
                .andExpect(status().isForbidden());

        String adminToken = login(createUser("platform-admin1@example.com", Role.PLATFORM_ADMIN));
        mockMvc.perform(patch("/api/providers/" + providerId + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"verified\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void providerCanUpdateOwnProfile() throws Exception {
        String token = login(createUser("prov4@example.com", Role.SERVICE_PROVIDER));
        mockMvc.perform(post("/api/providers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"CityMove","bio":"Original bio","categories":["MOVING"]}"""))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/providers/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"CityMove Express","bio":"Updated bio","serviceArea":"Baku",
                                 "categories":["MOVING","HANDYMAN"]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("CityMove Express"))
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    @Test
    void updateOwnProfileFailsWhenNoProfileExists() throws Exception {
        String token = login(createUser("prov5@example.com", Role.SERVICE_PROVIDER));

        mockMvc.perform(patch("/api/providers/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"No Profile Yet","categories":["CLEANING"]}"""))
                .andExpect(status().isNotFound());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Provider").lastName("Test").email(email)
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
