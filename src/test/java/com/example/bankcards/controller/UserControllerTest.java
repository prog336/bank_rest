package com.example.bankcards.controller;

import com.example.bankcards.config.TestConfig;
import com.example.bankcards.dto.PagedResponseDTO;
import com.example.bankcards.dto.UserFilterDTO;
import com.example.bankcards.dto.UserRequestDTO;
import com.example.bankcards.dto.UserResponseDTO;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private UUID userId;
  private UserResponseDTO userResponseDTO;
  private UserRequestDTO userRequestDTO;
  private PagedResponseDTO<UserResponseDTO> pagedResponse;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userResponseDTO = new UserResponseDTO(userId, "Test User", "+12345678901");
    userRequestDTO = new UserRequestDTO("Test User", "+12345678901", "Password123@");

    pagedResponse = new PagedResponseDTO<>(
      List.of(userResponseDTO),
      0,
      10,
      1,
      1,
      true,
      true
    );
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_NoFilters_Success() throws Exception {
    when(userService.getUsers(any(UserFilterDTO.class))).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(userId.toString()))
      .andExpect(jsonPath("$.content[0].fullName").value("Test User"))
      .andExpect(jsonPath("$.content[0].phoneNumber").value("+12345678901"))
      .andExpect(jsonPath("$.page").value(0))
      .andExpect(jsonPath("$.size").value(10))
      .andExpect(jsonPath("$.totalElements").value(1))
      .andExpect(jsonPath("$.totalPages").value(1))
      .andExpect(jsonPath("$.first").value(true))
      .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_ByFullName_Success() throws Exception {
    when(userService.getUsers(any(UserFilterDTO.class))).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/users")
        .param("fullName", "Test")
        .param("phoneNumber", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(userId.toString()))
      .andExpect(jsonPath("$.content[0].fullName").value("Test User"));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_ByPhoneNumber_Success() throws Exception {
    when(userService.getUsers(any(UserFilterDTO.class))).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "+123")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].phoneNumber").value("+12345678901"));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_ByFullNameAndPhoneNumber_Success() throws Exception {
    when(userService.getUsers(any(UserFilterDTO.class))).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/users")
        .param("fullName", "Test")
        .param("phoneNumber", "+123")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(userId.toString()));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_EmptyResult_Success() throws Exception {
    PagedResponseDTO<UserResponseDTO> emptyResponse = new PagedResponseDTO<>(
      Collections.emptyList(), 0, 10, 0, 0, true, true
    );
    when(userService.getUsers(any(UserFilterDTO.class))).thenReturn(emptyResponse);

    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").isArray())
      .andExpect(jsonPath("$.content").isEmpty())
      .andExpect(jsonPath("$.totalElements").value(0))
      .andExpect(jsonPath("$.totalPages").value(0));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_DefaultPagination_Success() throws Exception {
    when(userService.getUsers(any(UserFilterDTO.class))).thenReturn(pagedResponse);

    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content[0].id").value(userId.toString()));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_WithCustomPagination_Success() throws Exception {
    PagedResponseDTO<UserResponseDTO> customPagedResponse = new PagedResponseDTO<>(
      List.of(userResponseDTO, userResponseDTO),
      1,
      5,
      10,
      2,
      true,
      false
    );
    when(userService.getUsers(any(UserFilterDTO.class))).thenReturn(customPagedResponse);

    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
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

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_InvalidPagination_ReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
        .param("page", "-1")
        .param("size", "10"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUsers_InvalidSize_ReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
        .param("page", "0")
        .param("size", "0"))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUserById_Success() throws Exception {
    when(userService.getUserById(userId)).thenReturn(userResponseDTO);

    mockMvc.perform(get("/api/users/{userId}", userId))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(userId.toString()))
      .andExpect(jsonPath("$.fullName").value("Test User"))
      .andExpect(jsonPath("$.phoneNumber").value("+12345678901"));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void getUserById_NotFound_ReturnsNotFound() throws Exception {
    when(userService.getUserById(userId))
      .thenThrow(new com.example.bankcards.exception.ResourceNotFoundException("User not found"));

    mockMvc.perform(get("/api/users/{userId}", userId))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void createUser_Success() throws Exception {
    when(userService.createUser(any(UserRequestDTO.class))).thenReturn(userResponseDTO);

    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequestDTO)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(userId.toString()))
      .andExpect(jsonPath("$.fullName").value("Test User"))
      .andExpect(jsonPath("$.phoneNumber").value("+12345678901"));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void createUser_InvalidPhone_ReturnsBadRequest() throws Exception {
    UserRequestDTO invalidDTO = new UserRequestDTO("Test", "bad", "Password123@");

    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void createUser_WeakPassword_ReturnsBadRequest() throws Exception {
    UserRequestDTO invalidDTO = new UserRequestDTO("Test User", "+12345678901", "weak");

    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void createUser_EmptyFullName_ReturnsBadRequest() throws Exception {
    UserRequestDTO invalidDTO = new UserRequestDTO("", "+12345678901", "Password123@");

    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void createUser_EmptyPhoneNumber_ReturnsBadRequest() throws Exception {
    UserRequestDTO invalidDTO = new UserRequestDTO("Test User", "", "Password123@");

    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void updateUser_Success() throws Exception {
    UserResponseDTO updatedUser = new UserResponseDTO(userId, "Updated User", "+98765432109");
    when(userService.updateUser(eq(userId), any(UserRequestDTO.class))).thenReturn(updatedUser);

    mockMvc.perform(put("/api/users/{userId}", userId)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequestDTO)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(userId.toString()))
      .andExpect(jsonPath("$.fullName").value("Updated User"))
      .andExpect(jsonPath("$.phoneNumber").value("+98765432109"));
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void updateUser_NotFound_ReturnsNotFound() throws Exception {
    when(userService.updateUser(eq(userId), any(UserRequestDTO.class)))
      .thenThrow(new com.example.bankcards.exception.ResourceNotFoundException("User not found"));

    mockMvc.perform(put("/api/users/{userId}", userId)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequestDTO)))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void deleteUser_Success() throws Exception {
    when(userService.deleteUser(userId)).thenReturn(userId);

    mockMvc.perform(delete("/api/users/{userId}", userId).with(csrf()))
      .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = "ADMIN")
  void deleteUser_NotFound_ReturnsNotFound() throws Exception {
    when(userService.deleteUser(userId))
      .thenThrow(new com.example.bankcards.exception.ResourceNotFoundException("User not found"));

    mockMvc.perform(delete("/api/users/{userId}", userId).with(csrf()))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getUsers_Unauthenticated_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void createUser_Unauthenticated_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequestDTO)))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void getUsers_AsUser_ReturnsForbidden() throws Exception {
    mockMvc.perform(get("/api/users")
        .param("fullName", "")
        .param("phoneNumber", "")
        .param("page", "0")
        .param("size", "10"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = "USER")
  void createUser_AsUser_ReturnsForbidden() throws Exception {
    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequestDTO)))
      .andExpect(status().isForbidden());
  }
}