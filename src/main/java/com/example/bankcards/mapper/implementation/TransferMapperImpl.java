package com.example.bankcards.mapper.implementation;

import com.example.bankcards.dto.TransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.util.CardNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TransferMapperImpl implements TransferMapper {
  private final CardNumberService cardNumberService;

  @Override
  public TransferResponseDTO mapToResponseDTO(Transfer transfer, Card sourceCard, Card destinationCard){
    String sourceCardNumber = cardNumberService.createMask(sourceCard.getLastDigits());
    String destinationCardNumber = cardNumberService.createMask(destinationCard.getLastDigits());

    return new TransferResponseDTO(
      transfer.getId(),
      transfer.getAmount().toPlainString(),
      transfer.getDate(),
      sourceCardNumber,
      destinationCardNumber
    );
  }
}
