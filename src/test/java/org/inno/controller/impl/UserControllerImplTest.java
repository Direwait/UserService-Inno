package org.inno.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.inno.service.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext
class UserControllerImplTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDto testUser;

    @BeforeEach
    void setUp() throws Exception {
        testUser = new UserDto();
        testUser.setEmail("test-" + UUID.randomUUID() + "@mail.com");
        testUser.setName("name");
        testUser.setSurname("Test User");
        testUser.setBirthDate(LocalDate.now().minusYears(20));
    }

    private UserDto createTestUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        UserDto user = objectMapper.treeToValue(jsonNode, UserDto.class);

        if (jsonNode.has("id") && !jsonNode.get("id").isNull()) {
            UUID id = UUID.fromString(jsonNode.get("id").asText());
            user.setId(id);
        }

        return user;
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        testUser.setEmail("not-an-email");
        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UserDto savedUser = createTestUser();

        assertThat(savedUser.getId()).isNotNull();

        mockMvc.perform(get("/users/{userId}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()));
    }

    @Test
    void getUserById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/users/{userId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserById_ShouldReturnUpdatedUser() throws Exception {
        UserDto savedUser = createTestUser();
        assertThat(savedUser.getId()).isNotNull();

        savedUser.setEmail("updated@mail.com");

        String updateJson = objectMapper.writeValueAsString(savedUser);

        mockMvc.perform(put("/users/{userId}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@mail.com"));
    }

    @Test
    void getAllUsers_WithoutSearchTerm_ShouldReturnPage() throws Exception {
        createTestUser();

        MvcResult result = mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        JsonNode contentNode = jsonNode.get("content");
        List<UserDto> users = objectMapper.readValue(
                contentNode.toString(),
                new TypeReference<List<UserDto>>() {}
        );

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(1);

        int totalElements = jsonNode.get("totalElements").asInt();
        assertThat(totalElements).isGreaterThanOrEqualTo(1);
    }


    @Test
    void activateDeactivateUser_ShouldToggleStatus() throws Exception {
        UserDto savedUser = createTestUser();
        assertThat(savedUser.getId()).isNotNull();

        mockMvc.perform(patch("/users/active/{userId}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        mockMvc.perform(get("/users/{userId}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void activateDeactivateUser_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(patch("/users/active/{userId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_ShouldReturnNoContent() throws Exception {
        UserDto savedUser = createTestUser();
        assertThat(savedUser.getId()).isNotNull();

        mockMvc.perform(delete("/users/{userId}", savedUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{userId}", savedUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_WithNonExistentId_ShouldReturnNotFound() throws Exception {

        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/users/{userId}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}