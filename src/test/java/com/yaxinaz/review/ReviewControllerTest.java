package com.yaxinaz.review;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.provider.ProviderProfile;
import com.yaxinaz.provider.ServiceCategory;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void onlyTheRequestingCustomerCanReviewAndProviderRatingIsRecalculated() throws Exception {
        User customer = createUser("review-customer1@example.com", Role.RESIDENT);
        User someoneElse = createUser("review-outsider1@example.com", Role.RESIDENT);
        User providerUser = createUser("review-provider1@example.com", Role.SERVICE_PROVIDER);
        ProviderProfile provider = createProvider(providerUser, "TestFix", ServiceCategory.HANDYMAN);

        String customerToken = login(customer);
        String outsiderToken = login(someoneElse);
        String providerToken = login(providerUser);

        long requestId = createCompletedServiceRequest(provider, customerToken, providerToken);

        mockMvc.perform(post("/api/service-requests/" + requestId + "/reviews")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":1,\"comment\":\"Not my request\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/service-requests/" + requestId + "/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"Solid work\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4));

        mockMvc.perform(get("/api/providers/" + provider.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.reviewCount").value(1));

        mockMvc.perform(get("/api/providers/" + provider.getId() + "/reviews")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].rating").value(4))
                .andExpect(jsonPath("$.content[0].comment").value("Solid work"));
    }

    @Test
    void secondReviewAveragesWithTheFirst() throws Exception {
        User providerUser = createUser("review-provider2@example.com", Role.SERVICE_PROVIDER);
        ProviderProfile provider = createProvider(providerUser, "AverageFix", ServiceCategory.PLUMBING);
        String providerToken = login(providerUser);

        String firstCustomerToken = login(createUser("review-customer2a@example.com", Role.RESIDENT));
        long firstRequestId = createCompletedServiceRequest(provider, firstCustomerToken, providerToken);
        mockMvc.perform(post("/api/service-requests/" + firstRequestId + "/reviews")
                        .header("Authorization", "Bearer " + firstCustomerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isCreated());

        String secondCustomerToken = login(createUser("review-customer2b@example.com", Role.RESIDENT));
        long secondRequestId = createCompletedServiceRequest(provider, secondCustomerToken, providerToken);
        mockMvc.perform(post("/api/service-requests/" + secondRequestId + "/reviews")
                        .header("Authorization", "Bearer " + secondCustomerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":3}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/providers/" + provider.getId())
                        .header("Authorization", "Bearer " + secondCustomerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.0))
                .andExpect(jsonPath("$.reviewCount").value(2));
    }

    private long createCompletedServiceRequest(ProviderProfile provider, String customerToken, String providerToken) throws Exception {
        String response = mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerId":%d,"category":"%s","description":"Needs work"}"""
                                .formatted(provider.getId(), provider.getCategories().iterator().next())))
                .andReturn().getResponse().getContentAsString();
        long requestId = extractLong(response, "\"id\":");

        for (String next : new String[] {"ACCEPTED", "IN_PROGRESS", "COMPLETED"}) {
            mockMvc.perform(patch("/api/service-requests/" + requestId + "/status")
                            .header("Authorization", "Bearer " + providerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + next + "\"}"))
                    .andExpect(status().isOk());
        }
        return requestId;
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("Review").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private ProviderProfile createProvider(User user, String businessName, ServiceCategory category) {
        return providerProfileRepository.save(ProviderProfile.builder()
                .user(user).businessName(businessName).categories(Set.of(category))
                .verified(false).averageRating(0.0).reviewCount(0).build());
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
