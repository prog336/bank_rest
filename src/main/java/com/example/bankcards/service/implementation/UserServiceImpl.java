package com.example.bankcards.service.implementation;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final PageMapper pageMapper;
  private final RoleRepository roleRepository;

  @Override
  public PagedResponseDTO<UserResponseDTO> getUsers(UserFilterDTO userFilterDTO) {
    Pageable pageable = PageRequest.of(userFilterDTO.page(), userFilterDTO.size());
    Page<UserEntity> userPage;

    boolean hasFullName = userFilterDTO.fullName() != null && !userFilterDTO.fullName().isBlank();
    boolean hasPhoneNumber = userFilterDTO.phoneNumber() != null && !userFilterDTO.phoneNumber().isBlank();

    if (!hasFullName && !hasPhoneNumber) {
      userPage = userRepository.findAll(pageable);
    } else if (hasFullName && hasPhoneNumber) {
      userPage = userRepository.findByFullNameContainingAndPhoneNumberContaining(
        userFilterDTO.fullName(), userFilterDTO.phoneNumber(), pageable);
    } else if (hasFullName) {
      userPage = userRepository.findByFullNameContaining(userFilterDTO.fullName(), pageable);
    } else {
      userPage = userRepository.findByPhoneNumberContaining(userFilterDTO.phoneNumber(), pageable);
    }

    return pageMapper.mapToPagedResponseDTO(userPage, userMapper::mapToResponseDTO);
  }

  @Override
  public UserResponseDTO getUserById(UUID userId) {
    UserEntity user = userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    return userMapper.mapToResponseDTO(user);
  }

  @Override
  public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
    checkUserExistence(userRequestDTO.phoneNumber());

    UserEntity newUser = new UserEntity();
    newUser.setFullName(userRequestDTO.fullName());
    newUser.setPhoneNumber(userRequestDTO.phoneNumber());
    newUser.setPassword(passwordEncoder.encode(userRequestDTO.password()));
    Role role = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Can't find role: USER"));
    newUser.setRoles(Collections.singletonList(role));

    UserEntity createdUser = userRepository.save(newUser);

    return userMapper.mapToResponseDTO(createdUser);
  }

  @Override
  public UserResponseDTO updateUser(UUID userId, UserRequestDTO userRequestDTO) {
    UserEntity existingUser = userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    if (!existingUser.getPhoneNumber().equals(userRequestDTO.phoneNumber())){
      checkUserExistence(userRequestDTO.phoneNumber());
    }

    existingUser.setFullName(userRequestDTO.fullName());
    existingUser.setPhoneNumber(userRequestDTO.phoneNumber());
    existingUser.setPassword(passwordEncoder.encode(userRequestDTO.password()));

    UserEntity updatedUser = userRepository.save(existingUser);

    return userMapper.mapToResponseDTO(updatedUser);
  }

  @Override
  public UUID deleteUser(UUID userId) {
    UserEntity user = userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    userRepository.delete(user);

    return user.getId();
  }

  private void checkUserExistence(String phoneNumber){
    if (userRepository.existsByPhoneNumber(phoneNumber)) {
      throw new BadRequestException("User with phone number " + phoneNumber + " already exists");
    }
  }
}