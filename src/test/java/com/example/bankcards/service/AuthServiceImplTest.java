package com.example.bankcards.service;

import com.example.bankcards.dto.LoginDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JWTGenerator;
import com.example.bankcards.service.implementation.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JWTGenerator jwtGenerator;

  @InjectMocks
  private AuthServiceImpl authService;

  private UserEntity testUser;
  private LoginDTO loginDTO;
  private String testToken;

  @BeforeEach
  void setUp() {
    testUser = new UserEntity();
    testUser.setId(UUID.randomUUID());
    testUser.setPhoneNumber("+12345678901");
    testUser.setPassword("encodedPassword");
    testUser.setFullName("Test User");

    Role userRole = new Role();
    userRole.setId((short) 1);
    userRole.setName("USER");
    testUser.setRoles(List.of(userRole));

    loginDTO = new LoginDTO("+12345678901", "password123");
    testToken = "test.jwt.token";
  }

  @Test
  void loginUser_Success() {
    when(userRepository.findByPhoneNumber(loginDTO.phoneNumber()))
      .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginDTO.password(), testUser.getPassword()))
      .thenReturn(true);

    Authentication authentication = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
      .thenReturn(authentication);
    when(jwtGenerator.generateToken(authentication))
      .thenReturn(testToken);

    String result = authService.loginUser(loginDTO);

    assertNotNull(result);
    assertEquals(testToken, result);
    verify(userRepository).findByPhoneNumber(loginDTO.phoneNumber());
    verify(passwordEncoder).matches(loginDTO.password(), testUser.getPassword());
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtGenerator).generateToken(authentication);
  }

  @Test
  void loginUser_UserNotFound_ThrowsException() {
    when(userRepository.findByPhoneNumber(loginDTO.phoneNumber()))
      .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> authService.loginUser(loginDTO));
    verify(userRepository).findByPhoneNumber(loginDTO.phoneNumber());
    verifyNoInteractions(passwordEncoder, authenticationManager, jwtGenerator);
  }

  @Test
  void loginUser_WrongPassword_ThrowsException() {
    when(userRepository.findByPhoneNumber(loginDTO.phoneNumber()))
      .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginDTO.password(), testUser.getPassword()))
      .thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.loginUser(loginDTO));
    verify(userRepository).findByPhoneNumber(loginDTO.phoneNumber());
    verify(passwordEncoder).matches(loginDTO.password(), testUser.getPassword());
    verifyNoInteractions(authenticationManager, jwtGenerator);
  }

  @Test
  void loginUser_AuthenticationFails_ThrowsException() {
    when(userRepository.findByPhoneNumber(loginDTO.phoneNumber()))
      .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginDTO.password(), testUser.getPassword()))
      .thenReturn(true);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
      .thenThrow(new RuntimeException("Authentication failed"));

    assertThrows(RuntimeException.class, () -> authService.loginUser(loginDTO));
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verifyNoInteractions(jwtGenerator);
  }
}