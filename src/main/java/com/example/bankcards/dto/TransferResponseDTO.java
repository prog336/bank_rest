package com.example.bankcards.dto;

import java.time.Instant;
import java.util.UUID;

public record TransferResponseDTO(
  UUID id,
  String amount,
  Instant date,
  String sourceCardNumber,
  String destinationCardNumber
) {
}
