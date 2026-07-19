package com.example.bankcards.service;

import com.example.bankcards.dto.PagedResponseDTO;
import com.example.bankcards.dto.UserFilterDTO;
import com.example.bankcards.dto.UserRequestDTO;
import com.example.bankcards.dto.UserResponseDTO;

import java.util.UUID;

public interface UserService {
  PagedResponseDTO<UserResponseDTO> getUsers(UserFilterDTO userFilterDTO);

  UserResponseDTO getUserById(UUID userId);

  UserResponseDTO createUser(UserRequestDTO userRequestDTO);

  UserResponseDTO updateUser(UUID userId, UserRequestDTO userRequestDTO);

  UUID deleteUser(UUID userId);
}
