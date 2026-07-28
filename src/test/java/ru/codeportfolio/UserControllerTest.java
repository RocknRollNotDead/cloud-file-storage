package ru.codeportfolio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.codeportfolio.dao.UserRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// тест этих эндпоинтов
// /auth/
// /user/
// /admin-panel/

class UserControllerTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    String Alice = "Alice";
    String password = "password";


    private final String json = """
            {"username": "%s", "password": "%s"}
            """.formatted(Alice, password);

    @BeforeEach
    @Transactional
    void clean() {
        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    void shouldGetUserInfo() throws Exception {


        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(Alice));

        mockMvc.perform(get("/api/user/me")
                        .with(user(Alice).roles("USER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(Alice));

        assertThat(userRepository.findUsersByLogin(Alice)).isPresent();
    }

    @Test
    void shouldCreateUser() throws Exception {


        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(Alice));

        assertThat(userRepository.findUsersByLogin(Alice)).isPresent();
    }

    @Test
    void shouldNotCreateUser() throws Exception {


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

        assertThat(userRepository.findUsersByLogin(Alice)).isPresent();
    }

    @Test
    void shouldReturn401WhenUserNotAuth() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn403WhenUserWantGetListFromAdminPanel() throws Exception {

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));

        mockMvc.perform(get("/api/admin-panel/users")
                        .with(user("Alice").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn403WhenUserWantDeleteUserFromAdminPanel() throws Exception {

        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        mockMvc.perform(delete("/api/admin-panel/users/10")
                .with(user("Alice").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    //todo обработать исключения



}