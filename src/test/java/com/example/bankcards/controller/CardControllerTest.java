package com.example.bankcards.controller;

import com.example.bankcards.config.TestConfig;
import com.example.bankcards.dto.*;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class CardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private CardService cardService;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private UUID cardId;
  private CardResponseDTO cardResponseDTO;
  private CardCreateDTO cardCreateDTO;
  private PagedResponseDTO<CardResponseDTO> pagedResponse;

  @BeforeEach
  void setUp() {
    cardId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    LocalDate futureDate = LocalDate.now().plusYears(1);

    cardResponseDTO = new CardResponseDTO(
      cardId, "****-****-****-1234", futureDate,
      "ACTIVE", BigDecimal.valueOf(1000), "Test User", "+12345678901"
    );
    cardCreateDTO = new CardCreateDTO("1234567890123456", futureDate, userId);

    pagedResponse = new PagedResponseDTO<>(
      List.of(cardResponseDTO),
      0,
      10,
      1,
      1,
      true,
      true
    );
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void getCards_AsUser_ReturnsCards() throws Exception {
    when(cardService.getCards(any(CardFilterDTO.class), any())).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/cards")
        .param("status", "ACTIVE")
        .param("cardNumber", "")
        .param("ownerFullName", "")
        .param("lastDigits", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(cardId.toString()))
      .andExpect(jsonPath("$.page").value(0))
      .andExpect(jsonPath("$.size").value(10))
      .andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.totalPages").value(1))
      .andExpect(jsonPath("$.first").value(true))
      .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @WithMockUser(username = "admin", authorities = {"USER", "ADMIN"})
  void getCards_AsAdmin_ReturnsCards() throws Exception {
    when(cardService.getCards(any(CardFilterDTO.class), any())).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/cards")
        .param("status", "ACTIVE")
        .param("cardNumber", "")
        .param("ownerFullName", "")
        .param("lastDigits", "")
        .param("page", "0")
        .param("size", "20"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(cardId.toString()));
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void getCards_WithFilters_ReturnsFilteredCards() throws Exception {
    when(cardService.getCards(any(CardFilterDTO.class), any())).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/cards")
        .param("status", "ACTIVE")
        .param("cardNumber", "")
        .param("ownerFullName", "Test")
        .param("lastDigits", "1234")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(cardId.toString()));
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void getCards_EmptyResult_ReturnsEmptyPage() throws Exception {
    PagedResponseDTO<CardResponseDTO> emptyResponse = new PagedResponseDTO<>(
      Collections.emptyList(), 0, 10, 0, 0, true, true
    );
    when(cardService.getCards(any(CardFilterDTO.class), any())).thenReturn(emptyResponse);

    mockMvc.perform(get("/api/cards")
        .param("status", "ACTIVE")
        .param("cardNumber", "")
        .param("ownerFullName", "")
        .param("lastDigits", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray())
      .andExpect(jsonPath("$.content").isEmpty())
      .andExpect(jsonPath("$.totalElements").value(0))
      .andExpect(jsonPath("$.totalPages").value(0));
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void getCards_DefaultPagination_ReturnsCards() throws Exception {
    when(cardService.getCards(any(CardFilterDTO.class), any())).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/cards")
        .param("status", "ACTIVE")
        .param("cardNumber", "")
        .param("ownerFullName", "")
        .param("lastDigits", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(cardId.toString()));
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void getCardById_Success() throws Exception {
    when(cardService.getCardById(eq(cardId), any())).thenReturn(cardResponseDTO);

    mockMvc.perform(get("/api/cards/{cardId}", cardId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(cardId.toString()))
      .andExpect(jsonPath("$.status").value("ACTIVE"))
      .andExpect(jsonPath("$.balance").value(1000));
  }

  @Test
  @WithMockUser(username = "admin", authorities = {"USER", "ADMIN"})
  void getCardById_AsAdmin_Success() throws Exception {
    when(cardService.getCardById(eq(cardId), any())).thenReturn(cardResponseDTO);

    mockMvc.perform(get("/api/cards/{cardId}", cardId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(cardId.toString()));
  }

  @Test
  @WithMockUser(username = "admin", authorities = "ADMIN")
  void createCard_Success() throws Exception {
    when(cardService.createCard(any(CardCreateDTO.class))).thenReturn(cardResponseDTO);

    mockMvc.perform(post("/api/cards")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cardCreateDTO)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(cardId.toString()))
      .andExpect(jsonPath("$.status").value("ACTIVE"))
      .andExpect(jsonPath("$.balance").value(1000));
  }

  @Test
  @WithMockUser(username = "admin", authorities = "ADMIN")
  void createCard_InvalidCardNumber_ReturnsBadRequest() throws Exception {
    CardCreateDTO invalidDTO = new CardCreateDTO("123", LocalDate.now().plusYears(1), UUID.randomUUID());

    mockMvc.perform(post("/api/cards")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin", authorities = "ADMIN")
  void createCard_InvalidExpiryDate_ReturnsBadRequest() throws Exception {
    CardCreateDTO invalidDTO = new CardCreateDTO("1234567890123456", LocalDate.now().minusMonths(1), UUID.randomUUID());

    mockMvc.perform(post("/api/cards")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin", authorities = "ADMIN")
  void updateCard_Success() throws Exception {
    CardUpdateDTO updateDTO = new CardUpdateDTO(
      com.example.bankcards.enums.CardStatus.BLOCKED, "500.00"
    );
    CardResponseDTO updatedCard = new CardResponseDTO(
      cardId, "****-****-****-1234", LocalDate.now().plusYears(1),
      "BLOCKED", BigDecimal.valueOf(500), "Test User", "+12345678901"
    );

    when(cardService.updateCard(eq(cardId), any(CardUpdateDTO.class))).thenReturn(updatedCard);

    mockMvc.perform(put("/api/cards/{cardId}", cardId)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDTO)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("BLOCKED"))
      .andExpect(jsonPath("$.balance").value(500));
  }

  @Test
  @WithMockUser(username = "admin", authorities = "ADMIN")
  void deleteCard_Success() throws Exception {
    when(cardService.deleteCard(cardId)).thenReturn(cardId);

    mockMvc.perform(delete("/api/cards/{cardId}", cardId).with(csrf()))
      .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void createCardBlockRequest_Success() throws Exception {
    CardResponseDTO blockedCard = new CardResponseDTO(
      cardId, "****-****-****-1234", LocalDate.now().plusYears(1),
      "BLOCK_REQUEST", BigDecimal.valueOf(1000), "Test User", "+12345678901"
    );

    when(cardService.createCardBlockRequest(eq(cardId), any())).thenReturn(blockedCard);

    mockMvc.perform(post("/api/cards/{cardId}/block-request", cardId).with(csrf()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(cardId.toString()))
      .andExpect(jsonPath("$.status").value("BLOCK_REQUEST"));
  }

  @Test
  @WithAnonymousUser
  void getCards_Unauthenticated_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/cards")
        .param("status", "ACTIVE")
        .param("cardNumber", "")
        .param("ownerFullName", "")
        .param("lastDigits", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void createCard_Unauthenticated_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/cards")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cardCreateDTO)))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void createCard_AsUser_ReturnsForbidden() throws Exception {
    mockMvc.perform(post("/api/cards")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cardCreateDTO)))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "+12345678901", authorities = "USER")
  void getCardById_NotFound_ReturnsNotFound() throws Exception {
    when(cardService.getCardById(eq(cardId), any()))
      .thenThrow(new com.example.bankcards.exception.ResourceNotFoundException("Card not found"));

    mockMvc.perform(get("/api/cards/{cardId}", cardId))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getCardById_Unauthenticated_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/cards/{cardId}", cardId))
      .andExpect(status().isUnauthorized());
  }
}