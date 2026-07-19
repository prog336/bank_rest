package com.example.bankcards.util;

public interface CardNumberService {
  String hashCardNumber(String cardNumber);

  String extractLastDigits(String cardNumber);

  String createMask(String lastDigits);
}
