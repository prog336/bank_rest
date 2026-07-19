package com.example.bankcards.controller;

import com.example.bankcards.config.TestConfig;
import com.example.bankcards.dto.LoginDTO;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AuthService authService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
  }

  @Test
  void loginUser_Success() throws Exception {
    String testToken = "eyJhbGciOiJIUzI1NiJ9.testToken";
    when(authService.loginUser(any(LoginDTO.class))).thenReturn(testToken);

    LoginDTO loginDTO = new LoginDTO("+12345678901", "password123");
    mockMvc.perform(post("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDTO)))
      .andExpect(status().isOk())
      .andExpect(content().string(testToken));
  }

  @Test
  void loginUser_InvalidPhoneNumber_ReturnsBadRequest() throws Exception {
    LoginDTO invalidLoginDTO = new LoginDTO("invalid-phone", "password123");

    mockMvc.perform(post("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
      .andExpect(status().isBadRequest());
  }

  @Test
  void loginUser_BlankPassword_ReturnsBadRequest() throws Exception {
    LoginDTO invalidLoginDTO = new LoginDTO("+12345678901", "");

    mockMvc.perform(post("/api/auth/login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
      .andExpect(status().isBadRequest());
  }
}