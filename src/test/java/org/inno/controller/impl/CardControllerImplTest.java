package org.inno.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.inno.service.dto.CardDto;
import org.inno.service.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
class CardControllerImplTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto testUser;
    private CardDto testCard;

    @BeforeEach
    void setUp() throws Exception {
        testUser = new UserDto();
        testUser.setName("CardTestUser");
        testUser.setSurname("Test Surname");
        testUser.setBirthDate(LocalDate.now().minusYears(25));
        testUser.setEmail("card-test-" + UUID.randomUUID() + "@mail.com");
        testUser = createUser(testUser);

        testCard = new CardDto();
        testCard.setNumber("1234567890123456");
        testCard.setExpirationDate(LocalDate.now().plusYears(3));
        testCard.setHolder("Holder");
        testCard.setUserId(testUser.getId());
    }

    private UserDto createUser(UserDto user) throws Exception {
        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        UserDto createdUser = objectMapper.treeToValue(jsonNode, UserDto.class);

        if (jsonNode.has("id")) {
            createdUser.setId(UUID.fromString(jsonNode.get("id").asText()));
        }

        return createdUser;
    }

    private CardDto createCard(CardDto card) throws Exception {
        MvcResult result = mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("Create card response: " + jsonResponse);

        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        CardDto createdCard = objectMapper.treeToValue(jsonNode, CardDto.class);

        if (jsonNode.has("id") && !jsonNode.get("id").isNull()) {
            createdCard.setId(UUID.fromString(jsonNode.get("id").asText()));
        }

        System.out.println("Created card ID: " + createdCard.getId());
        return createdCard;
    }

    @Test
    void createCard_ShouldReturnCreatedCard() throws Exception {
        String cardJson = objectMapper.writeValueAsString(testCard);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(testCard.getNumber()))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createCard_WithInvalidNumber_ShouldReturnBadRequest() throws Exception {
        testCard.setNumber("123");
        String cardJson = objectMapper.writeValueAsString(testCard);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_ShouldReturnCard() throws Exception {
        CardDto savedCard = createCard(testCard);

        mockMvc.perform(get("/cards/{cardId}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId().toString()))
                .andExpect(jsonPath("$.number").value(savedCard.getNumber()));
    }

    @Test
    void getCardById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/cards/{cardId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCardById_ShouldReturnUpdatedCard() throws Exception {
        CardDto savedCard = createCard(testCard);
        savedCard.setNumber("1111222233334444");

        String updateJson = objectMapper.writeValueAsString(savedCard);

        mockMvc.perform(put("/cards/{cardId}", savedCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1111222233334444"));
    }

    @Test
    void getAllCards_WithoutFilter_ShouldReturnPage() throws Exception {
        createCard(testCard);

        MvcResult result = mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        JsonNode contentNode = jsonNode.get("content");
        List<CardDto> users = objectMapper.readValue(
                contentNode.toString(),
                new TypeReference<List<CardDto>>() {}
        );

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getAllCardsByUserId_ShouldReturnList() throws Exception {
        createCard(testCard);

        UserDto otherUser = new UserDto();
        otherUser.setName("Other");
        otherUser.setSurname("User");
        otherUser.setBirthDate(LocalDate.now().minusYears(30));
        otherUser.setEmail("other-" + UUID.randomUUID() + "@mail.com");
        otherUser = createUser(otherUser);

        testCard.setUserId(otherUser.getId());
        createCard(testCard);


        MvcResult result = mockMvc.perform(get("/cards/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<CardDto> cards = objectMapper.readValue(
                jsonResponse,
                new TypeReference<List<CardDto>>() {}
        );

        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    void getAllCardsByUserId_WithNonExistentUser_ShouldReturnEmptyList() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(get("/cards/user/{userId}", nonExistentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void activateDeactivateCard_ShouldToggleStatus() throws Exception {
        CardDto savedCard = createCard(testCard);

        mockMvc.perform(patch("/cards/active/{cardId}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        mockMvc.perform(get("/cards/{cardId}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void activateDeactivateCard_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(patch("/cards/active/{cardId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_ShouldReturnNoContent() throws Exception {
        CardDto savedCard = createCard(testCard);

        mockMvc.perform(delete("/cards/{cardId}", savedCard.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/cards/{cardId}", savedCard.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/cards/{cardId}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}