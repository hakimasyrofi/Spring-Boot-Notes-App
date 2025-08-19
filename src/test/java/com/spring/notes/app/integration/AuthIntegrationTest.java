package com.spring.notes.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.notes.app.dto.request.LoginRequest;
import com.spring.notes.app.dto.request.RegisterRequest;
import com.spring.notes.app.entity.User;
import com.spring.notes.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testRegisterAndLoginFlow() throws Exception {
        // Step 1: Register a new user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .password("password123")
                .firstName("Integration")
                .lastName("User")
                .build();

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.username").value("integrationuser"))
                .andExpect(jsonPath("$.data.user.email").value("integration@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify user was saved in database
        User savedUser = userRepository.findByUsername("integrationuser").orElse(null);
        assert savedUser != null;
        assertEquals("integrationuser", savedUser.getUsername());
        assertEquals("integration@example.com", savedUser.getEmail());

        // Step 2: Login with the registered user
        LoginRequest loginRequest = LoginRequest.builder()
                .username("integrationuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.username").value("integrationuser"));
    }

    @Test
    void testRegister_DuplicateUsername() throws Exception {
        // First registration
        RegisterRequest registerRequest1 = RegisterRequest.builder()
                .username("duplicateuser")
                .email("user1@example.com")
                .password("password123")
                .firstName("User")
                .lastName("One")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest1)))
                .andExpect(status().isCreated());

        // Second registration with same username
        RegisterRequest registerRequest2 = RegisterRequest.builder()
                .username("duplicateuser")
                .email("user2@example.com")
                .password("password123")
                .firstName("User")
                .lastName("Two")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void testRegister_DuplicateEmail() throws Exception {
        // First registration
        RegisterRequest registerRequest1 = RegisterRequest.builder()
                .username("user1")
                .email("duplicate@example.com")
                .password("password123")
                .firstName("User")
                .lastName("One")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest1)))
                .andExpect(status().isCreated());

        // Second registration with same email
        RegisterRequest registerRequest2 = RegisterRequest.builder()
                .username("user2")
                .email("duplicate@example.com")
                .password("password123")
                .firstName("User")
                .lastName("Two")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Try to login with non-existent user
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonexistent")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        // Test with missing required fields
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("")
                .email("invalid-email")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        // Test with missing required fields
        LoginRequest invalidRequest = LoginRequest.builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
