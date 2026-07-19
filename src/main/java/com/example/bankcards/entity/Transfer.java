package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transfers")
public class Transfer {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "date", nullable = false)
  private Instant date;

  @ManyToOne
  @JoinColumn(name = "source_card_id", nullable = false)
  private Card sourceCard;

  @ManyToOne
  @JoinColumn(name = "destination_card_id", nullable = false)
  private Card destinationCard;
}
