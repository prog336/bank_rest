package com.example.bankcards.exception;

public class CardInactiveException extends BadRequestException {
  public CardInactiveException(String message) {
    super(message);
  }
}
