package com.yaxinaz.storage;

import com.yaxinaz.AbstractIntegrationTest;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void authenticatedUserCanUploadAnAllowedImage() throws Exception {
        String token = login(createUser("upload-resident1@example.com"));
        MockMultipartFile image = new MockMultipartFile(
                "file", "photo.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3, 4});

        mockMvc.perform(multipart("/api/files").file(image).header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.startsWith("/uploads/")))
                .andExpect(jsonPath("$.filename").value(org.hamcrest.Matchers.endsWith(".png")))
                .andExpect(jsonPath("$.sizeBytes").value(4));
    }

    @Test
    void rejectsDisallowedFileExtension() throws Exception {
        String token = login(createUser("upload-resident2@example.com"));
        MockMultipartFile executable = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/api/files").file(executable).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "file", "photo.png", MediaType.IMAGE_PNG_VALUE, new byte[] {1, 2, 3, 4});

        mockMvc.perform(multipart("/api/files").file(image))
                .andExpect(status().isUnauthorized());
    }

    private User createUser(String email) {
        return userRepository.save(User.builder()
                .firstName("Upload").lastName("Test").email(email)
                .password(passwordEncoder.encode("password123"))
                .role(Role.RESIDENT).preferredLanguage("EN").enabled(true).build());
    }

    private String login(User user) throws Exception {
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + user.getEmail() + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return response.split("\"token\":\"")[1].split("\"")[0];
    }
}
