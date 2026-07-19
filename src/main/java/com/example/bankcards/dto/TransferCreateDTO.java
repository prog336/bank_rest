package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record TransferCreateDTO(
  @NotNull(message = "Amount is required")
  @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Amount must be a valid number with up to 2 decimal places")
  @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
  @Digits(integer = 10, fraction = 2, message = "Amount cannot exceed 10 digits")
  String amount,

  @NotNull(message = "Source card ID is required")
  UUID sourceCardId,

  @NotNull(message = "Destination card ID is required")
  UUID destinationCardId
) {
}
