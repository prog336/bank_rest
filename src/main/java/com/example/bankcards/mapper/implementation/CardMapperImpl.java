package com.example.bankcards.mapper.implementation;

import com.example.bankcards.dto.CardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.util.CardNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CardMapperImpl implements CardMapper {
  private final CardNumberService cardNumberService;

  @Override
  public CardResponseDTO mapToResponseDTO(Card card){
    UserEntity owner = card.getOwner();
    String maskedCardNumber = cardNumberService.createMask(card.getLastDigits());

    return new CardResponseDTO(
      card.getId(),
      maskedCardNumber,
      card.getExpiryDate(),
      card.getStatus(),
      card.getBalance(),
      owner.getFullName(),
      owner.getPhoneNumber()
    );
  }
}
