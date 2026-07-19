package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
  boolean existsByCardNumberHash(String cardNumberHash);

  Page<Card> findByCardNumberHash(String cardNumberHash, Pageable pageable);

  Page<Card> findByCardNumberHashAndOwnerPhoneNumber(String cardNumberHash, String ownerPhoneNumber, Pageable pageable);

  Page<Card> findByStatus(String status, Pageable pageable);

  Page<Card> findByOwnerFullNameContainingIgnoreCaseAndLastDigitsContainingAndStatus(
    String ownerFullName, String lastDigits, String status, Pageable pageable);

  Page<Card> findByOwnerFullNameContainingIgnoreCaseAndStatus(String ownerFullName, String status, Pageable pageable);

  Page<Card> findByLastDigitsContainingAndStatus(String lastDigits, String status, Pageable pageable);

  Page<Card> findByOwnerPhoneNumberAndLastDigitsContainingAndStatus(
    String ownerPhoneNumber, String lastDigits, String status, Pageable pageable);

  Page<Card> findByOwnerPhoneNumberAndStatus(String ownerPhoneNumber, String status, Pageable pageable);
}