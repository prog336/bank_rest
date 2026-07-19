package com.example.bankcards.dto;

import java.util.UUID;

public record UserResponseDTO(
  UUID id,
  String fullName,
  String phoneNumber
) {
}
