package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferFilterDTO;
import com.example.bankcards.dto.TransferCreateDTO;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/transfers")
@AllArgsConstructor
public class TransferController {
  private final TransferService transferService;

  @GetMapping
  public ResponseEntity<?> getTransfers (TransferFilterDTO transferFilterDTO, Authentication authentication){
    return ResponseEntity.ok(transferService.getTransfers(transferFilterDTO, authentication));
  }

  @PostMapping
  public ResponseEntity<?> createTransfer (@Valid @RequestBody TransferCreateDTO transferCreateDTO, Authentication authentication){
    return ResponseEntity.status(HttpStatus.CREATED).body(transferService.createTransfer(transferCreateDTO, authentication));
  }
}