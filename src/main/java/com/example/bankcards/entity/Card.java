package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cards")
public class Card {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "card_number_hash", columnDefinition = "text", unique = true, nullable = false)
  private String cardNumberHash;

  @Column(columnDefinition = "text", nullable = false)
  private String lastDigits;

  @Column(name = "expiry_date", nullable = false)
  private LocalDate expiryDate;

  @Column(columnDefinition = "text", nullable = false)
  private String status;

  @Column(nullable = false)
  private BigDecimal balance;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id", nullable = false)
  private UserEntity owner;

  @OneToMany(mappedBy = "sourceCard", orphanRemoval = true)
  private List<Transfer> outgoingTransfers;

  @OneToMany(mappedBy = "destinationCard", orphanRemoval = true)
  private List<Transfer> incomingTransfers;
}
