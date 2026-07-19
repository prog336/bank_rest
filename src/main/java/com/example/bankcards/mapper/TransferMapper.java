package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;

public interface TransferMapper {
  TransferResponseDTO mapToResponseDTO(Transfer transfer, Card sourceCard, Card destinationCard);
}
