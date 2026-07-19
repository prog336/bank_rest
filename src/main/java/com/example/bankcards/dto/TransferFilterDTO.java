package com.example.bankcards.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferFilterDTO(
  UUID sourceCardId,
  UUID destinationCardId,
  
  @NotNull(message = "Page number is required")
  @Min(value = 0, message = "Page number must be positive")
  Integer page,

  @NotNull(message = "Page size is required")
  @Min(value = 1, message = "Page size must be at least 1")
  Integer size
) {
}
