package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
  Page<Transfer> findBySourceCardIdAndDestinationCardId(UUID sourceCardId, UUID destinationCardId, Pageable pageable);

  Page<Transfer> findBySourceCardId(UUID sourceCardId, Pageable pageable);

  Page<Transfer> findByDestinationCardId(UUID destinationCardId, Pageable pageable);
}