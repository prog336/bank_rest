package com.example.bankcards.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record CardCreateDTO(
  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits long")
  String cardNumber,

  @NotNull(message = "Expiry date is required")
  @FutureOrPresent(message = "Expiry date must be in the present or future")
  @JsonFormat(pattern = "yyyy-MM-dd")
  LocalDate expiryDate,

  @NotNull(message = "Owner id is required")
  UUID ownerId
) {
}
