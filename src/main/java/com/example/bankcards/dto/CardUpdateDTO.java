  package com.example.bankcards.dto;

  import com.example.bankcards.enums.CardStatus;
  import jakarta.validation.constraints.NotNull;
  import jakarta.validation.constraints.Pattern;

  public record CardUpdateDTO(
    CardStatus status,

    @NotNull(message = "Balance should not be null")
    @Pattern(regexp = "^(\\d+(\\.\\d{1,2})?)?$", message = "Balance must be a valid number with up to 2 decimal places")
    String balance
  ) {
  }
