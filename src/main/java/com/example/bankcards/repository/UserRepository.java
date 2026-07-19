package com.example.bankcards.repository;

import com.example.bankcards.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  boolean existsByPhoneNumber(String phoneNumber);

  Optional<UserEntity> findByPhoneNumber(String phoneNumber);

  Page<UserEntity> findByFullNameContaining(String fullName, Pageable pageable);

  Page<UserEntity> findByPhoneNumberContaining(String phoneNumber, Pageable pageable);

  Page<UserEntity> findByFullNameContainingAndPhoneNumberContaining(
    String fullName, String phoneNumber, Pageable pageable);
}
