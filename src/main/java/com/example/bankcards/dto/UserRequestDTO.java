package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequestDTO(
  @NotBlank(message = "Full name should not be blank")
  String fullName,

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^\\+[0-9]{11}$", message = "Phone number must be 12 characters starting with +")
  String phoneNumber,

  @NotBlank(message = "Password is required")
  @Pattern(
    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=?!]).{8,100}$",
    message = "Password must be 8-100 characters and contain at least one digit, one lowercase, one uppercase, and one special character"
  )
  String password
) {
}
