package com.example.bankcards.util.implementation;

import com.example.bankcards.util.CardNumberService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CardNumberServiceImpl implements CardNumberService {
  @Value("${card.encryption.secret-key}")
  private String secretKey;

  @Override
  public String hashCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.isBlank()) {
      throw new IllegalArgumentException("Card number cannot be null or empty");
    }

    String cleanedCardNumber = cardNumber.strip().replaceAll("\\s+", "");

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(
        secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
      );
      mac.init(secretKeySpec);

      byte[] hash = mac.doFinal(cleanedCardNumber.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);

    } catch (Exception e) {
      throw new RuntimeException("Error hashing card number", e);
    }
  }

  @Override
  public String extractLastDigits(String cardNumber) {
    if (cardNumber == null || cardNumber.length() < 4) {
      throw new IllegalArgumentException("Card number must have at least 4 digits");
    }

    String cleanedCardNumber = cardNumber.strip().replaceAll("\\s+", "");

    return cleanedCardNumber.substring(cleanedCardNumber.length() - 4);
  }

  @Override
  public String createMask(String lastDigits) {
    if (lastDigits == null || lastDigits.length() != 4) {
      throw new IllegalArgumentException("Last digits must be 4 digits long");
    }

    return "**** ".repeat(3) + lastDigits;
  }
}
