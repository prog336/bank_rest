package com.example.bankcards.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserFilterDTO(
  String fullName,

  @Pattern(regexp = "^(\\+?[0-9]{0,12})?$", message = "Phone number must be empty or contain 0-11 digits with or without +")
  String phoneNumber,

  @NotNull(message = "Page number is required")
  @Min(value = 0, message = "Page number must be positive")
  Integer page,

  @NotNull(message = "Page size is required")
  @Min(value = 1, message = "Page size must be at least 1")
  Integer size
) {
}
