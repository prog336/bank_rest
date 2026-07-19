package com.example.bankcards.mapper.implementation;

import com.example.bankcards.dto.UserResponseDTO;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {
  @Override
  public UserResponseDTO mapToResponseDTO(UserEntity user){
    return new UserResponseDTO(
      user.getId(),
      user.getFullName(),
      user.getPhoneNumber()
    );
  }
}
