package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardResponseDTO(
  UUID id,
  String cardNumber,
  LocalDate expiryDate,
  String status,
  BigDecimal balance,
  String ownerFullName,
  String ownerPhoneNumber
) {
}
