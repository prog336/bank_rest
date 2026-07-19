package com.example.bankcards.service;

import com.example.bankcards.dto.PagedResponseDTO;
import com.example.bankcards.dto.TransferCreateDTO;
import com.example.bankcards.dto.TransferFilterDTO;
import com.example.bankcards.dto.TransferResponseDTO;
import org.springframework.security.core.Authentication;

public interface TransferService {
  PagedResponseDTO<TransferResponseDTO> getTransfers(TransferFilterDTO transferFilterDTO, Authentication authentication);

  TransferResponseDTO createTransfer(TransferCreateDTO transferCreateDTO, Authentication authentication);
}
