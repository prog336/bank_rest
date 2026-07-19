package com.example.bankcards.controller;

import com.example.bankcards.config.TestConfig;
import com.example.bankcards.dto.PagedResponseDTO;
import com.example.bankcards.dto.TransferCreateDTO;
import com.example.bankcards.dto.TransferFilterDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.service.TransferService;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class TransferControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TransferService transferService;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private UUID sourceCardId;
  private UUID destinationCardId;
  private TransferResponseDTO transferResponseDTO;
  private TransferCreateDTO transferCreateDTO;
  private PagedResponseDTO<TransferResponseDTO> pagedResponse;

  @BeforeEach
  void setUp() {
    sourceCardId = UUID.randomUUID();
    destinationCardId = UUID.randomUUID();

    transferResponseDTO = new TransferResponseDTO(
      UUID.randomUUID(), "100.00", Instant.now(), "****-1234", "****-5678"
    );
    transferCreateDTO = new TransferCreateDTO("100.00", sourceCardId, destinationCardId);

    pagedResponse = new PagedResponseDTO<>(
      List.of(transferResponseDTO),
      0,
      10,
      1,
      1,
      true,
      true
    );
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_BySourceCardId_Success() throws Exception {
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(pagedResponse);

    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString())
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(transferResponseDTO.id().toString()))
      .andExpect(jsonPath("$.content[0].amount").value("100.00"))
      .andExpect(jsonPath("$.page").value(0))
      .andExpect(jsonPath("$.size").value(10))
      .andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.totalPages").value(1))
      .andExpect(jsonPath("$.first").value(true))
      .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_ByDestinationCardId_Success() throws Exception {
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(pagedResponse);

    mockMvc.perform(get("/api/transfers")
        .param("destinationCardId", destinationCardId.toString())
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(transferResponseDTO.id().toString()));
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_ByBothCards_Success() throws Exception {
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(pagedResponse);

    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString())
        .param("destinationCardId", destinationCardId.toString())
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(transferResponseDTO.id().toString()));
  }

  @Test
  @WithMockUser(authorities = {"USER", "ADMIN"})
  void getTransfers_AsAdmin_Success() throws Exception {
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(pagedResponse);

    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString())
        .param("page", "0")
        .param("size", "20"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(transferResponseDTO.id().toString()));
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_EmptyResult_ReturnsEmptyPage() throws Exception {
    PagedResponseDTO<TransferResponseDTO> emptyResponse = new PagedResponseDTO<>(
      Collections.emptyList(), 0, 10, 0, 0, true, true
    );
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(emptyResponse);

    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString())
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray())
      .andExpect(jsonPath("$.content").isEmpty())
      .andExpect(jsonPath("$.totalElements").value(0))
      .andExpect(jsonPath("$.totalPages").value(0));
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_DefaultPagination_Success() throws Exception {
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(pagedResponse);

    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(transferResponseDTO.id().toString()));
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_InvalidPagination_ReturnsOk() throws Exception {
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(pagedResponse);

    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString())
        .param("page", "-1")
        .param("size", "10"))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void createTransfer_Success() throws Exception {
    when(transferService.createTransfer(any(TransferCreateDTO.class), any()))
      .thenReturn(transferResponseDTO);

    mockMvc.perform(post("/api/transfers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transferCreateDTO)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(transferResponseDTO.id().toString()))
      .andExpect(jsonPath("$.amount").value("100.00"))
      .andExpect(jsonPath("$.sourceCardNumber").value("****-1234"))
      .andExpect(jsonPath("$.destinationCardNumber").value("****-5678"));
  }

  @Test
  @WithMockUser(authorities = "USER")
  void createTransfer_InvalidAmount_ReturnsBadRequest() throws Exception {
    TransferCreateDTO invalidDTO = new TransferCreateDTO("0.00", sourceCardId, destinationCardId);

    mockMvc.perform(post("/api/transfers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void createTransfer_NegativeAmount_ReturnsBadRequest() throws Exception {
    TransferCreateDTO invalidDTO = new TransferCreateDTO("-100.00", sourceCardId, destinationCardId);

    mockMvc.perform(post("/api/transfers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void createTransfer_MissingSourceCard_ReturnsBadRequest() throws Exception {
    TransferCreateDTO invalidDTO = new TransferCreateDTO("100.00", null, destinationCardId);

    mockMvc.perform(post("/api/transfers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void createTransfer_MissingDestinationCard_ReturnsBadRequest() throws Exception {
    TransferCreateDTO invalidDTO = new TransferCreateDTO("100.00", sourceCardId, null);

    mockMvc.perform(post("/api/transfers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void getTransfers_Unauthenticated_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString())
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void createTransfer_Unauthenticated_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/transfers")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transferCreateDTO)))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_MissingCardIds_ReturnsOk() throws Exception {
    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenThrow(new com.example.bankcards.exception.BadRequestException("Required at least one of those: source card id / destination card id"));

    mockMvc.perform(get("/api/transfers")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getTransfers_WithCustomPagination_Success() throws Exception {
    PagedResponseDTO<TransferResponseDTO> customPagedResponse = new PagedResponseDTO<>(
      List.of(transferResponseDTO, transferResponseDTO),
      1,
      5,
      10,
      2,
      true,
      false
    );

    when(transferService.getTransfers(any(TransferFilterDTO.class), any()))
      .thenReturn(customPagedResponse);

    mockMvc.perform(get("/api/transfers")
        .param("sourceCardId", sourceCardId.toString())
        .param("page", "1")
        .param("size", "5"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content.length()").value(2))
      .andExpect(jsonPath("$.page").value(1))
      .andExpect(jsonPath("$.size").value(5))
      .andExpect(jsonPath("$.totalElements").value(10))
      .andExpect(jsonPath("$.totalPages").value(2))
      .andExpect(jsonPath("$.first").value(false))
      .andExpect(jsonPath("$.last").value(true));
  }
}