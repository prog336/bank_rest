package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PageMapper pageMapper;

  @InjectMocks
  private UserServiceImpl userService;

  private UserEntity testUser;
  private UserResponseDTO testUserResponse;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();

    testUser = new UserEntity();
    testUser.setId(userId);
    testUser.setFullName("Test User");
    testUser.setPhoneNumber("+12345678901");
    testUser.setPassword("encodedPassword");

    testUserResponse = new UserResponseDTO(
      userId,
      "Test User",
      "+12345678901"
    );
  }

  private PagedResponseDTO<UserResponseDTO> createPagedResponse(List<UserResponseDTO> content, int page, int size) {
    return new PagedResponseDTO<>(
      content,
      page,
      size,
      content.size(),
      content.isEmpty() ? 0 : 1,
      true,
      true
    );
  }

  @Test
  void getUsers_NoFilters_ReturnsAllUsers() {
    UserFilterDTO filterDTO = new UserFilterDTO("", "", 0, 10);

    Page<UserEntity> userPage = new PageImpl<>(List.of(testUser));
    PagedResponseDTO<UserResponseDTO> expectedResponse = createPagedResponse(
      List.of(testUserResponse), 0, 10
    );

    when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
    when(pageMapper.mapToPagedResponseDTO(eq(userPage), ArgumentMatchers.<Function<UserEntity, UserResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<UserResponseDTO> result = userService.getUsers(filterDTO);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
    assertEquals(testUserResponse, result.content().getFirst());
    verify(userRepository).findAll(any(Pageable.class));
    verify(pageMapper).mapToPagedResponseDTO(eq(userPage), ArgumentMatchers.<Function<UserEntity, UserResponseDTO>>any());
  }

  @Test
  void getUsers_ByFullNameFilter_ReturnsFilteredUsers() {
    UserFilterDTO filterDTO = new UserFilterDTO("Test", "", 0, 10);

    Page<UserEntity> userPage = new PageImpl<>(List.of(testUser));
    PagedResponseDTO<UserResponseDTO> expectedResponse = createPagedResponse(
      List.of(testUserResponse), 0, 10
    );

    when(userRepository.findByFullNameContaining(eq("Test"), any(Pageable.class)))
      .thenReturn(userPage);
    when(pageMapper.mapToPagedResponseDTO(eq(userPage), ArgumentMatchers.<Function<UserEntity, UserResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<UserResponseDTO> result = userService.getUsers(filterDTO);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
    verify(userRepository).findByFullNameContaining(eq("Test"), any(Pageable.class));
  }

  @Test
  void getUsers_ByPhoneFilter_ReturnsFilteredUsers() {
    UserFilterDTO filterDTO = new UserFilterDTO("", "+123", 0, 10);

    Page<UserEntity> userPage = new PageImpl<>(List.of(testUser));
    PagedResponseDTO<UserResponseDTO> expectedResponse = createPagedResponse(
      List.of(testUserResponse), 0, 10
    );

    when(userRepository.findByPhoneNumberContaining(eq("+123"), any(Pageable.class)))
      .thenReturn(userPage);
    when(pageMapper.mapToPagedResponseDTO(eq(userPage), ArgumentMatchers.<Function<UserEntity, UserResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<UserResponseDTO> result = userService.getUsers(filterDTO);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
    verify(userRepository).findByPhoneNumberContaining(eq("+123"), any(Pageable.class));
  }

  @Test
  void getUsers_ByFullNameAndPhoneFilter_ReturnsFilteredUsers() {
    UserFilterDTO filterDTO = new UserFilterDTO("Test", "+123", 0, 10);

    Page<UserEntity> userPage = new PageImpl<>(List.of(testUser));
    PagedResponseDTO<UserResponseDTO> expectedResponse = createPagedResponse(
      List.of(testUserResponse), 0, 10
    );

    when(userRepository.findByFullNameContainingAndPhoneNumberContaining(
      eq("Test"), eq("+123"), any(Pageable.class)))
      .thenReturn(userPage);
    when(pageMapper.mapToPagedResponseDTO(eq(userPage), ArgumentMatchers.<Function<UserEntity, UserResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<UserResponseDTO> result = userService.getUsers(filterDTO);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
    verify(userRepository).findByFullNameContainingAndPhoneNumberContaining(
      eq("Test"), eq("+123"), any(Pageable.class));
  }

  @Test
  void getUsers_EmptyResult_ReturnsEmptyPage() {
    UserFilterDTO filterDTO = new UserFilterDTO("", "", 0, 10);

    Page<UserEntity> emptyPage = new PageImpl<>(Collections.emptyList());
    PagedResponseDTO<UserResponseDTO> expectedResponse = createPagedResponse(
      Collections.emptyList(), 0, 10
    );

    when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
    when(pageMapper.mapToPagedResponseDTO(eq(emptyPage), ArgumentMatchers.<Function<UserEntity, UserResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<UserResponseDTO> result = userService.getUsers(filterDTO);

    assertNotNull(result);
    assertEquals(0, result.totalElements());
    assertTrue(result.content().isEmpty());
  }

  @Test
  void getUsers_WithCustomPagination_ReturnsCorrectPage() {
    UserFilterDTO filterDTO = new UserFilterDTO("", "", 1, 5);

    Page<UserEntity> userPage = new PageImpl<>(
      List.of(testUser),
      org.springframework.data.domain.PageRequest.of(1, 5),
      1
    );
    PagedResponseDTO<UserResponseDTO> expectedResponse = new PagedResponseDTO<>(
      List.of(testUserResponse), 1, 5, 1, 1, true, false
    );

    when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
    when(pageMapper.mapToPagedResponseDTO(eq(userPage), ArgumentMatchers.<Function<UserEntity, UserResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<UserResponseDTO> result = userService.getUsers(filterDTO);

    assertNotNull(result);
    assertEquals(1, result.page());
    assertEquals(5, result.size());
    assertFalse(result.first());
    assertTrue(result.last());
  }

  @Test
  void getUserById_Success() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userMapper.mapToResponseDTO(testUser)).thenReturn(testUserResponse);

    UserResponseDTO result = userService.getUserById(userId);

    assertNotNull(result);
    assertEquals(testUserResponse.id(), result.id());
    assertEquals(testUserResponse.fullName(), result.fullName());
    assertEquals(testUserResponse.phoneNumber(), result.phoneNumber());
    verify(userRepository).findById(userId);
  }

  @Test
  void getUserById_NotFound_ThrowsException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    verify(userRepository).findById(userId);
  }

  @Test
  void createUser_Success() {
    UserRequestDTO requestDTO = new UserRequestDTO(
      "New User",
      "+98765432109",
      "Password123!"
    );

    Role userRole = new Role();
    userRole.setName("USER");

    when(userRepository.existsByPhoneNumber("+98765432109")).thenReturn(false);
    when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
    when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
    when(userMapper.mapToResponseDTO(testUser)).thenReturn(testUserResponse);

    UserResponseDTO result = userService.createUser(requestDTO);

    assertNotNull(result);
    assertEquals(testUserResponse, result);
    verify(userRepository).existsByPhoneNumber("+98765432109");
    verify(passwordEncoder).encode("Password123!");
    verify(roleRepository).findByName("USER");
    verify(userRepository).save(any(UserEntity.class));
    verify(userMapper).mapToResponseDTO(testUser);
  }

  @Test
  void createUser_PhoneNumberAlreadyExists_ThrowsException() {
    String existingPhoneNumber = "+12345678901";
    UserRequestDTO requestDTO = new UserRequestDTO(
      "New User",
      existingPhoneNumber,
      "Password123!"
    );

    when(userRepository.existsByPhoneNumber(existingPhoneNumber))
      .thenReturn(true);

    BadRequestException exception = assertThrows(
      BadRequestException.class,
      () -> userService.createUser(requestDTO)
    );

    assertEquals("User with phone number " + existingPhoneNumber + " already exists",
      exception.getMessage());

    verify(userRepository).existsByPhoneNumber(existingPhoneNumber);
    verify(userRepository, never()).save(any(UserEntity.class));
    verify(userMapper, never()).mapToResponseDTO(any(UserEntity.class));
    verify(passwordEncoder, never()).encode(any());
    verify(roleRepository, never()).findByName(any());
  }

  @Test
  void updateUser_Success() {
    UserRequestDTO requestDTO = new UserRequestDTO(
      "Updated User",
      "+98765432109",
      "NewPassword123!"
    );

    when(userRepository.existsByPhoneNumber("+98765432109")).thenReturn(false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedNewPassword");
    when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
    when(userMapper.mapToResponseDTO(testUser)).thenReturn(testUserResponse);

    UserResponseDTO result = userService.updateUser(userId, requestDTO);

    assertNotNull(result);
    assertEquals("Updated User", testUser.getFullName());
    assertEquals("+98765432109", testUser.getPhoneNumber());
    assertEquals("encodedNewPassword", testUser.getPassword());
    verify(userRepository).existsByPhoneNumber("+98765432109");
    verify(userRepository).findById(userId);
    verify(passwordEncoder).encode("NewPassword123!");
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  void updateUser_PhoneNumberAlreadyExists_ThrowsException() {
    String existingPhoneNumber = "+98765432109";
    UserRequestDTO requestDTO = new UserRequestDTO(
      "Updated User",
      existingPhoneNumber,
      "NewPassword123!"
    );

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    testUser.setPhoneNumber("+11111111111");

    when(userRepository.existsByPhoneNumber(existingPhoneNumber)).thenReturn(true);

    BadRequestException exception = assertThrows(
      BadRequestException.class,
      () -> userService.updateUser(userId, requestDTO)
    );

    assertEquals("User with phone number " + existingPhoneNumber + " already exists",
      exception.getMessage());

    verify(userRepository).existsByPhoneNumber(existingPhoneNumber);
    verify(userRepository, never()).save(any(UserEntity.class));
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  void updateUser_NotFound_ThrowsException() {
    UserRequestDTO requestDTO = new UserRequestDTO(
      "Updated User",
      "+98765432109",
      "NewPassword123!"
    );

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () ->
      userService.updateUser(userId, requestDTO));
    verify(userRepository).findById(userId);
    verify(userRepository, never()).save(any());
  }

  @Test
  void deleteUser_Success() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    doNothing().when(userRepository).delete(testUser);

    UUID result = userService.deleteUser(userId);

    assertEquals(userId, result);
    verify(userRepository).findById(userId);
    verify(userRepository).delete(testUser);
  }

  @Test
  void deleteUser_NotFound_ThrowsException() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));
    verify(userRepository).findById(userId);
    verify(userRepository, never()).delete(any());
  }
}