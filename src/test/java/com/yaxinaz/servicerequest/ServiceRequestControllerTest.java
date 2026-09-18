package com.yaxinaz.servicerequest;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceRequestControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void fullLifecycleFromRequestToCompletedWithReview() throws Exception {
        User customer = createUser("sr-customer1@example.com", Role.RESIDENT);
        User providerUser = createUser("sr-provider1@example.com", Role.SERVICE_PROVIDER);
        ProviderProfile provider = createProvider(providerUser, "SmartFix Appliances", ServiceCategory.APPLIANCE_REPAIR);

        String customerToken = login(customer);
        String providerToken = login(providerUser);

        String response = mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerId":%d,"category":"APPLIANCE_REPAIR","description":"Washing machine is leaking"}"""
                                .formatted(provider.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andReturn().getResponse().getContentAsString();
        long requestId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/service-requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(patch("/api/service-requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/service-requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(post("/api/service-requests/" + requestId + "/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Great job!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(post("/api/service-requests/" + requestId + "/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4}"))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotReviewBeforeCompletion() throws Exception {
        User customer = createUser("sr-customer2@example.com", Role.RESIDENT);
        User providerUser = createUser("sr-provider2@example.com", Role.SERVICE_PROVIDER);
        ProviderProfile provider = createProvider(providerUser, "CityMove", ServiceCategory.MOVING);
        String customerToken = login(customer);

        String response = mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerId":%d,"category":"MOVING","description":"Need help moving furniture"}"""
                                .formatted(provider.getId())))
                .andReturn().getResponse().getContentAsString();
        long requestId = extractLong(response, "\"id\":");

        mockMvc.perform(post("/api/service-requests/" + requestId + "/reviews")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidTransitionIsRejected() throws Exception {
        User customer = createUser("sr-customer3@example.com", Role.RESIDENT);
        User providerUser = createUser("sr-provider3@example.com", Role.SERVICE_PROVIDER);
        ProviderProfile provider = createProvider(providerUser, "PetCare Baku", ServiceCategory.PET_CARE);
        String customerToken = login(customer);
        String providerToken = login(providerUser);

        String response = mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerId":%d,"category":"PET_CARE","description":"Need dog walking"}"""
                                .formatted(provider.getId())))
                .andReturn().getResponse().getContentAsString();
        long requestId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/service-requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anotherProviderCannotActOnSomeoneElsesRequest() throws Exception {
        User customer = createUser("sr-customer4@example.com", Role.RESIDENT);
        User providerUser = createUser("sr-provider4a@example.com", Role.SERVICE_PROVIDER);
        User otherProviderUser = createUser("sr-provider4b@example.com", Role.SERVICE_PROVIDER);
        ProviderProfile provider = createProvider(providerUser, "TutorPro", ServiceCategory.TUTORING);
        createProvider(otherProviderUser, "OtherTutors", ServiceCategory.TUTORING);
        String customerToken = login(customer);
        String otherProviderToken = login(otherProviderUser);

        String response = mockMvc.perform(post("/api/service-requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerId":%d,"category":"TUTORING","description":"Math tutoring needed"}"""
                                .formatted(provider.getId())))
                .andReturn().getResponse().getContentAsString();
        long requestId = extractLong(response, "\"id\":");

        mockMvc.perform(patch("/api/service-requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + otherProviderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACCEPTED\"}"))
                .andExpect(status().isForbidden());
    }

    private User createUser(String email, Role role) {
        return userRepository.save(User.builder()
                .firstName("SR").lastName("Test").email(email)
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
