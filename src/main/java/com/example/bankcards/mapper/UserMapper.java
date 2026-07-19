package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserResponseDTO;
import com.example.bankcards.entity.UserEntity;

public interface UserMapper {
  UserResponseDTO mapToResponseDTO(UserEntity user);
}
