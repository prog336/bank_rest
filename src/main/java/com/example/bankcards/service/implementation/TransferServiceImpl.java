package com.example.bankcards.service.implementation;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.*;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.TransferService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@AllArgsConstructor
public class TransferServiceImpl implements TransferService {
  private final TransferRepository transferRepository;
  private final CardRepository cardRepository;
  private final TransferMapper transferMapper;
  private final PageMapper pageMapper;

  @Override
  public PagedResponseDTO<TransferResponseDTO> getTransfers(TransferFilterDTO transferFilterDTO, Authentication authentication) {
    if (transferFilterDTO.sourceCardId() == null && transferFilterDTO.destinationCardId() == null) {
      throw new BadRequestException("Required at least one of those: source card id / destination card id");
    }

    Pageable pageable = PageRequest.of(transferFilterDTO.page(), transferFilterDTO.size());
    Page<Transfer> transferPage;

    if (transferFilterDTO.sourceCardId() != null && transferFilterDTO.destinationCardId() != null) {
      Card sourceCard = cardRepository.findById(transferFilterDTO.sourceCardId())
        .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferFilterDTO.sourceCardId()));
      Card destinationCard = cardRepository.findById(transferFilterDTO.destinationCardId())
        .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferFilterDTO.destinationCardId()));
      checkCardsOwners(sourceCard, destinationCard, authentication);

      transferPage = transferRepository.findBySourceCardIdAndDestinationCardId(
        sourceCard.getId(), destinationCard.getId(), pageable);

      return pageMapper.mapToPagedResponseDTO(transferPage, transfer ->
        transferMapper.mapToResponseDTO(transfer, sourceCard, destinationCard));
    }

    if (transferFilterDTO.sourceCardId() != null) {
      Card sourceCard = cardRepository.findById(transferFilterDTO.sourceCardId())
        .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferFilterDTO.sourceCardId()));
      checkCardOwner(sourceCard, authentication);

      transferPage = transferRepository.findBySourceCardId(sourceCard.getId(), pageable);

      return pageMapper.mapToPagedResponseDTO(transferPage, transfer -> {
        Card destinationCard = transfer.getDestinationCard();
        return transferMapper.mapToResponseDTO(transfer, sourceCard, destinationCard);
      });
    }

    Card destinationCard = cardRepository.findById(transferFilterDTO.destinationCardId())
      .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferFilterDTO.destinationCardId()));
    checkCardOwner(destinationCard, authentication);

    transferPage = transferRepository.findByDestinationCardId(destinationCard.getId(), pageable);

    return pageMapper.mapToPagedResponseDTO(transferPage, transfer -> {
      Card sourceCard = transfer.getSourceCard();
      return transferMapper.mapToResponseDTO(transfer, sourceCard, destinationCard);
    });
  }

  @Override
  @Transactional
  public TransferResponseDTO createTransfer(TransferCreateDTO transferCreateDTO, Authentication authentication) {
    if (transferCreateDTO.destinationCardId().equals(transferCreateDTO.sourceCardId())){
      throw new BadRequestException("Source and destination card can`t be the same");
    }

    Card sourceCard = cardRepository.findById(transferCreateDTO.sourceCardId())
      .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferCreateDTO.sourceCardId()));
    Card destinationCard = cardRepository.findById(transferCreateDTO.destinationCardId())
      .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + transferCreateDTO.destinationCardId()));
    checkCardsOwners(sourceCard, destinationCard, authentication);

    if (!sourceCard.getStatus().equals("ACTIVE") || !destinationCard.getStatus().equals("ACTIVE")){
      throw new CardInactiveException("Source or destination card is inactive");
    }

    if (sourceCard.getBalance().compareTo(new BigDecimal(transferCreateDTO.amount())) < 0) {
      throw new InsufficientBalanceException("Insufficient balance");
    }

    sourceCard.setBalance(sourceCard.getBalance().subtract(new BigDecimal(transferCreateDTO.amount())));
    destinationCard.setBalance(destinationCard.getBalance().add(new BigDecimal(transferCreateDTO.amount())));

    cardRepository.save(sourceCard);
    cardRepository.save(destinationCard);

    Transfer newTransfer = new Transfer();
    newTransfer.setAmount(new BigDecimal(transferCreateDTO.amount()));
    newTransfer.setDate(Instant.now());
    newTransfer.setSourceCard(sourceCard);
    newTransfer.setDestinationCard(destinationCard);

    Transfer createdTransfer = transferRepository.save(newTransfer);

    return transferMapper.mapToResponseDTO(createdTransfer, sourceCard, destinationCard);
  }

  private void checkCardsOwners(Card sourceCard, Card destinationCard, Authentication authentication) {
    if (authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch("ADMIN"::equals) &&
      !(sourceCard.getOwner().getPhoneNumber().equals(authentication.getName())
        && destinationCard.getOwner().getPhoneNumber().equals(authentication.getName()))) {
      throw new UnauthorizedException("You don't own those cards");
    }
  }

  private void checkCardOwner(Card card, Authentication authentication) {
    if (authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch("ADMIN"::equals)
      && !card.getOwner().getPhoneNumber().equals(authentication.getName())) {
      throw new UnauthorizedException("You don't own this card");
    }
  }
}