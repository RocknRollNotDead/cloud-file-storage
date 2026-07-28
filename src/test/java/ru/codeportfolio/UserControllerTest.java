package ru.codeportfolio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import ru.codeportfolio.dao.UserRepository;
import ru.codeportfolio.model.Role;
import ru.codeportfolio.model.User;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

class UserControllerTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();

    }

    @Test
    void shouldGetUserInfo() throws Exception {
        String json = """
            {"username": "user1", "password": "PASSWORD_USER"}
            """;

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"));

        mockMvc.perform(get("/api/user/me")
                        .with(httpBasic("user1", "PASSWORD_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));

        assertThat(userRepository.findUsersByLogin("user1")).isPresent();
    }

    @Test
    void shouldCreateUser() throws Exception {
        String json = """
            {"username": "Alice", "password": "password"}
            """;

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Alice"));

        assertThat(userRepository.findUsersByLogin("Alice")).isPresent();
    }

    @Test
    void shouldNotCreateUser() throws Exception {
        String json = """
            {"username": "user1", "password": "password"}
            """;

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").exists());

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        assertThat(userRepository.findUsersByLogin("user1")).isPresent();
    }

    @Test
    void shouldReturn401WhenUserNotAuth() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn403WhenUserWantGetListFromAdminPanel() throws Exception {

        String json = """
            {"username": "Alice", "password": "password"}
            """;

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        mockMvc.perform(get("/api/admin-panel/users")
                        .with(httpBasic("Alice", "password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn403WhenUserWantDeleteUserFromAdminPanel() throws Exception {
        String json = """
            {"username": "Alice", "password": "password"}
            """;
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        mockMvc.perform(delete("/api/admin-panel/users/10")
                .with(httpBasic("Alice", "password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }




}