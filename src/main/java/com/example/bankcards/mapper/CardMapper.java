package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;

public interface CardMapper {
  CardResponseDTO mapToResponseDTO(Card card);
}
