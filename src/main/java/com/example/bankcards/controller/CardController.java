package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/cards")
@AllArgsConstructor
public class CardController {
  private final CardService cardService;

  @GetMapping
  public ResponseEntity<?> getCards (@Valid CardFilterDTO cardFilterDTO, Authentication authentication){
    return ResponseEntity.ok(cardService.getCards(cardFilterDTO, authentication));
  }

  @GetMapping("/{cardId}")
  public ResponseEntity<?> getCardById (@PathVariable UUID cardId, Authentication authentication){
    return ResponseEntity.ok(cardService.getCardById(cardId, authentication));
  }

  @PostMapping
  public ResponseEntity<?> createCard (@Valid @RequestBody CardCreateDTO cardCreateDTO){
    return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(cardCreateDTO));
  }

  @PutMapping("/{cardId}")
  public ResponseEntity<?> updateCard (@PathVariable UUID cardId, @Valid @RequestBody CardUpdateDTO cardUpdateDTO){
    return ResponseEntity.ok(cardService.updateCard(cardId, cardUpdateDTO));
  }

  @DeleteMapping("/{cardId}")
  public ResponseEntity<?> deleteCard (@PathVariable UUID cardId){
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(cardService.deleteCard(cardId));
  }

  @PostMapping("/{cardId}/block-request")
  public ResponseEntity<?> createCardBlockRequest (@PathVariable UUID cardId, Authentication authentication){
    return ResponseEntity.ok(cardService.createCardBlockRequest(cardId, authentication));
  }
}