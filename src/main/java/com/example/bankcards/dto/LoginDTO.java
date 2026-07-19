package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginDTO(
  @NotBlank(message = "Phone number is required")
  @Pattern(
    regexp = "^\\+[0-9]{11}$",
    message = "Phone number must be 12 characters starting with +"
  )
  String phoneNumber,

  @NotBlank(message = "Password is required")
  String password
) {
}
