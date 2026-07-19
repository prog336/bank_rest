package com.example.bankcards.service.implementation;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardNumberService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {
  private final CardRepository cardRepository;
  private final UserRepository userRepository;
  private final CardNumberService cardNumberService;
  private final CardMapper cardMapper;
  private final PageMapper pageMapper;

  @Override
  public PagedResponseDTO<CardResponseDTO> getCards(CardFilterDTO cardFilterDTO, Authentication authentication) {
    Pageable pageable = PageRequest.of(cardFilterDTO.page(), cardFilterDTO.size());
    Page<Card> cardPage;

    boolean isAdmin = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ADMIN"::equals);
    boolean hasCardNumber = cardFilterDTO.cardNumber() != null && !cardFilterDTO.cardNumber().isBlank();
    boolean hasOwnerFullName = cardFilterDTO.ownerFullName() != null && !cardFilterDTO.ownerFullName().isBlank();
    boolean hasLastDigits = cardFilterDTO.lastDigits() != null && !cardFilterDTO.lastDigits().isBlank();

    if (isAdmin) {
      if (hasCardNumber) {
        if (cardFilterDTO.cardNumber().length() < 16 || cardFilterDTO.cardNumber().length() > 19){
          throw new BadRequestException("Card number should be 16-19 digits long");
        }
        cardPage = cardRepository.findByCardNumberHash(cardNumberService.hashCardNumber(
          cardFilterDTO.cardNumber()), PageRequest.of(0, 1));
      } else if (hasOwnerFullName && hasLastDigits) {
        if (cardFilterDTO.lastDigits().length() > 4) throw new BadRequestException("Last digits must be 4 digits long or shorter");
        cardPage = cardRepository.findByOwnerFullNameContainingIgnoreCaseAndLastDigitsContainingAndStatus(
          cardFilterDTO.ownerFullName(), cardFilterDTO.lastDigits(), cardFilterDTO.status().name(), pageable);
      } else if (hasOwnerFullName) {
        cardPage = cardRepository.findByOwnerFullNameContainingIgnoreCaseAndStatus(
          cardFilterDTO.ownerFullName(), cardFilterDTO.status().name(), pageable);
      } else if (hasLastDigits) {
        if (cardFilterDTO.lastDigits().length() > 4) throw new BadRequestException("Last digits must be 4 digits long or shorter");
        cardPage = cardRepository.findByLastDigitsContainingAndStatus(
          cardFilterDTO.lastDigits(), cardFilterDTO.status().name(), pageable);
      } else {
        cardPage = cardRepository.findByStatus(cardFilterDTO.status().name(), pageable);
      }
    } else {
      if (hasCardNumber) {
        if (cardFilterDTO.cardNumber().length() < 16 || cardFilterDTO.cardNumber().length() > 19){
          throw new BadRequestException("Card number should be 16-19 digits long");
        }
        cardPage = cardRepository.findByCardNumberHashAndOwnerPhoneNumber(cardNumberService.hashCardNumber(
          cardFilterDTO.cardNumber()), authentication.getName(), PageRequest.of(0, 1));
      } else if (hasLastDigits) {
        if (cardFilterDTO.lastDigits().length() > 4) throw new BadRequestException("Last digits must be 4 digits long or shorter");
        cardPage = cardRepository.findByOwnerPhoneNumberAndLastDigitsContainingAndStatus(
          authentication.getName(), cardFilterDTO.lastDigits(), cardFilterDTO.status().name(), pageable);
      } else {
        cardPage = cardRepository.findByOwnerPhoneNumberAndStatus(
          authentication.getName(), cardFilterDTO.status().name(), pageable);
      }
    }

    return pageMapper.mapToPagedResponseDTO(cardPage, cardMapper::mapToResponseDTO);
  }

  @Override
  public CardResponseDTO getCardById(UUID cardId, Authentication authentication) {
    Card card = cardRepository.findById(cardId)
      .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
    checkCardOwner(card, authentication);

    return cardMapper.mapToResponseDTO(card);
  }

  @Override
  public CardResponseDTO createCard(CardCreateDTO cardCreateDTO) {
    checkCardExistence(cardCreateDTO.cardNumber());

    UserEntity owner = userRepository.findById(cardCreateDTO.ownerId())
      .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + cardCreateDTO.ownerId()));

    Card newCard = new Card();
    newCard.setCardNumberHash(cardNumberService.hashCardNumber(cardCreateDTO.cardNumber()));
    newCard.setLastDigits(cardNumberService.extractLastDigits(cardCreateDTO.cardNumber()));
    newCard.setExpiryDate(cardCreateDTO.expiryDate());
    newCard.setStatus("ACTIVE");
    newCard.setBalance(BigDecimal.ZERO);
    newCard.setOwner(owner);

    Card createdCard = cardRepository.save(newCard);

    return cardMapper.mapToResponseDTO(createdCard);
  }

  @Override
  public CardResponseDTO updateCard(UUID cardId, CardUpdateDTO cardUpdateDTO) {

    Card existingCard = cardRepository.findById(cardId)
      .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

    if (cardUpdateDTO.status() != null) existingCard.setStatus(cardUpdateDTO.status().name());
    if (!cardUpdateDTO.balance().isBlank()) existingCard.setBalance(new BigDecimal(cardUpdateDTO.balance()));

    Card updatedCard = cardRepository.save(existingCard);

    return cardMapper.mapToResponseDTO(updatedCard);
  }

  @Override
  public UUID deleteCard(UUID cardId) {
    Card card = cardRepository.findById(cardId)
      .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
    cardRepository.delete(card);

    return card.getId();
  }

  @Override
  public CardResponseDTO createCardBlockRequest(UUID cardId, Authentication authentication) {
    Card existingCard = cardRepository.findById(cardId)
      .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
    checkCardOwner(existingCard, authentication);

    if (!existingCard.getStatus().equals("ACTIVE")){
      throw new BadRequestException("Cant block inactive card");
    }

    existingCard.setStatus("BLOCK_REQUEST");

    Card updatedCard = cardRepository.save(existingCard);

    return cardMapper.mapToResponseDTO(updatedCard);
  }

  private void checkCardExistence(String cardNumber){
    String cardNumberHash = cardNumberService.hashCardNumber(cardNumber);
    if (cardRepository.existsByCardNumberHash(cardNumberHash)) {
      throw new BadRequestException("Card with card number " + cardNumber + " already exists");
    }
  }

  private void checkCardOwner(Card card, Authentication authentication) {
    if (authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch("ADMIN"::equals)
      && !card.getOwner().getPhoneNumber().equals(authentication.getName())) {
      throw new UnauthorizedException("You don't own this card");
    }
  }
}