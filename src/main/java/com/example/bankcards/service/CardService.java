package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface CardService {
  PagedResponseDTO<CardResponseDTO> getCards(CardFilterDTO cardFilterDTO, Authentication authentication);

  CardResponseDTO getCardById(UUID cardId, Authentication authentication);

  CardResponseDTO createCard(CardCreateDTO cardCreateDTO);

  CardResponseDTO updateCard(UUID cardId, CardUpdateDTO cardUpdateDTO);

  UUID deleteCard(UUID cardId);

  CardResponseDTO createCardBlockRequest(UUID cardId, Authentication authentication);
}
