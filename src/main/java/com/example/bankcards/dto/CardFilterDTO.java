package com.example.bankcards.dto;

import com.example.bankcards.enums.CardStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CardFilterDTO(
  @NotNull(message = "Card status is required")
  CardStatus status,

  @Pattern(regexp = "^(\\d{16})?$", message = "Card number must be 16 digits long or empty")
  String cardNumber,

  String ownerFullName,

  @Pattern(regexp = "^(\\d{1,4})?$", message = "Last digits must be empty or 1 to 4 digits")
  String lastDigits,

  @NotNull(message = "Page number is required")
  @Min(value = 0, message = "Page number must be positive")
  Integer page,

  @NotNull(message = "Page size is required")
  @Min(value = 1, message = "Page size must be at least 1")
  Integer size
) {
}
