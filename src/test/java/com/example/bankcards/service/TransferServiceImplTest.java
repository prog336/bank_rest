package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.implementation.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

  @Mock
  private TransferRepository transferRepository;

  @Mock
  private CardRepository cardRepository;

  @Mock
  private TransferMapper transferMapper;

  @Mock
  private PageMapper pageMapper;

  @InjectMocks
  private TransferServiceImpl transferService;

  private Card sourceCard;
  private Card destinationCard;
  private Transfer testTransfer;
  private TransferResponseDTO testTransferResponse;
  private Authentication userAuth;
  private Authentication adminAuth;
  private UUID sourceCardId;
  private UUID destinationCardId;
  private UUID transferId;

  @BeforeEach
  void setUp() {
    sourceCardId = UUID.randomUUID();
    destinationCardId = UUID.randomUUID();
    transferId = UUID.randomUUID();

    UserEntity sourceUser = new UserEntity();
    sourceUser.setId(UUID.randomUUID());
    sourceUser.setPhoneNumber("+12345678901");
    sourceUser.setFullName("Source User");

    UserEntity testUser = new UserEntity();
    testUser.setId(UUID.randomUUID());
    testUser.setPhoneNumber("+12345678901");
    testUser.setFullName("Test User");

    sourceCard = new Card();
    sourceCard.setId(sourceCardId);
    sourceCard.setCardNumberHash("hash1");
    sourceCard.setLastDigits("1234");
    sourceCard.setBalance(BigDecimal.valueOf(1000));
    sourceCard.setOwner(sourceUser);
    sourceCard.setStatus("ACTIVE");

    destinationCard = new Card();
    destinationCard.setId(destinationCardId);
    destinationCard.setCardNumberHash("hash2");
    destinationCard.setLastDigits("5678");
    destinationCard.setBalance(BigDecimal.valueOf(500));
    destinationCard.setOwner(testUser);
    destinationCard.setStatus("ACTIVE");

    testTransfer = new Transfer();
    testTransfer.setId(transferId);
    testTransfer.setAmount(BigDecimal.valueOf(100));
    testTransfer.setDate(Instant.now());
    testTransfer.setSourceCard(sourceCard);
    testTransfer.setDestinationCard(destinationCard);

    testTransferResponse = new TransferResponseDTO(
      transferId,
      "100.00",
      testTransfer.getDate(),
      "****-1234",
      "****-5678"
    );

    userAuth = mock(Authentication.class);
    adminAuth = mock(Authentication.class);
  }

  private void setupUserAuth() {
    lenient().when(userAuth.getName()).thenReturn("+12345678901");
    lenient().when(userAuth.getAuthorities()).thenAnswer(invocation ->
      Collections.singletonList(new SimpleGrantedAuthority("USER")));
  }

  private void setupAdminAuth() {
    lenient().when(adminAuth.getName()).thenReturn("admin");
    lenient().when(adminAuth.getAuthorities()).thenAnswer(invocation ->
      Collections.singletonList(new SimpleGrantedAuthority("ADMIN")));
  }

  private PagedResponseDTO<TransferResponseDTO> createPagedResponse(List<TransferResponseDTO> content, int page, int size) {
    return new PagedResponseDTO<>(
      content,
      page,
      size,
      content.size(),
      content.isEmpty() ? 0 : 1,
      true,
      true
    );
  }

  @Test
  void getTransfers_BySourceCardId_Success() {
    setupUserAuth();
    TransferFilterDTO filterDTO = new TransferFilterDTO(sourceCardId, null, 0, 10);

    Page<Transfer> transferPage = new PageImpl<>(List.of(testTransfer));
    PagedResponseDTO<TransferResponseDTO> expectedResponse = createPagedResponse(
      List.of(testTransferResponse), 0, 10
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(transferRepository.findBySourceCardId(eq(sourceCardId), any(Pageable.class)))
      .thenReturn(transferPage);
    when(pageMapper.mapToPagedResponseDTO(eq(transferPage), ArgumentMatchers.<Function<Transfer, TransferResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<TransferResponseDTO> result = transferService.getTransfers(filterDTO, userAuth);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
    verify(cardRepository).findById(sourceCardId);
    verify(transferRepository).findBySourceCardId(eq(sourceCardId), any(Pageable.class));
    verify(pageMapper).mapToPagedResponseDTO(eq(transferPage), ArgumentMatchers.<Function<Transfer, TransferResponseDTO>>any());
  }

  @Test
  void getTransfers_ByDestinationCardId_Success() {
    setupUserAuth();
    TransferFilterDTO filterDTO = new TransferFilterDTO(null, destinationCardId, 0, 10);

    Page<Transfer> transferPage = new PageImpl<>(List.of(testTransfer));
    PagedResponseDTO<TransferResponseDTO> expectedResponse = createPagedResponse(
      List.of(testTransferResponse), 0, 10
    );

    when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));
    when(transferRepository.findByDestinationCardId(eq(destinationCardId), any(Pageable.class)))
      .thenReturn(transferPage);
    when(pageMapper.mapToPagedResponseDTO(eq(transferPage), ArgumentMatchers.<Function<Transfer, TransferResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<TransferResponseDTO> result = transferService.getTransfers(filterDTO, userAuth);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    verify(cardRepository).findById(destinationCardId);
    verify(transferRepository).findByDestinationCardId(eq(destinationCardId), any(Pageable.class));
  }

  @Test
  void getTransfers_ByBothCards_Success() {
    setupUserAuth();
    TransferFilterDTO filterDTO = new TransferFilterDTO(sourceCardId, destinationCardId, 0, 10);

    Page<Transfer> transferPage = new PageImpl<>(List.of(testTransfer));
    PagedResponseDTO<TransferResponseDTO> expectedResponse = createPagedResponse(
      List.of(testTransferResponse), 0, 10
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));
    when(transferRepository.findBySourceCardIdAndDestinationCardId(
      eq(sourceCardId), eq(destinationCardId), any(Pageable.class)))
      .thenReturn(transferPage);
    when(pageMapper.mapToPagedResponseDTO(eq(transferPage), ArgumentMatchers.<Function<Transfer, TransferResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<TransferResponseDTO> result = transferService.getTransfers(filterDTO, userAuth);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    verify(cardRepository).findById(sourceCardId);
    verify(cardRepository).findById(destinationCardId);
    verify(transferRepository).findBySourceCardIdAndDestinationCardId(
      eq(sourceCardId), eq(destinationCardId), any(Pageable.class));
  }

  @Test
  void getTransfers_AsAdmin_BySourceCardId_Success() {
    setupAdminAuth();
    TransferFilterDTO filterDTO = new TransferFilterDTO(sourceCardId, null, 0, 10);

    Page<Transfer> transferPage = new PageImpl<>(List.of(testTransfer));
    PagedResponseDTO<TransferResponseDTO> expectedResponse = createPagedResponse(
      List.of(testTransferResponse), 0, 10
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(transferRepository.findBySourceCardId(eq(sourceCardId), any(Pageable.class)))
      .thenReturn(transferPage);
    when(pageMapper.mapToPagedResponseDTO(eq(transferPage), ArgumentMatchers.<Function<Transfer, TransferResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<TransferResponseDTO> result = transferService.getTransfers(filterDTO, adminAuth);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    verify(cardRepository).findById(sourceCardId);
    verify(transferRepository).findBySourceCardId(eq(sourceCardId), any(Pageable.class));
  }

  @Test
  void getTransfers_EmptyResult_ReturnsEmptyPage() {
    setupUserAuth();
    TransferFilterDTO filterDTO = new TransferFilterDTO(sourceCardId, null, 0, 10);

    Page<Transfer> emptyPage = new PageImpl<>(Collections.emptyList());
    PagedResponseDTO<TransferResponseDTO> expectedResponse = createPagedResponse(
      Collections.emptyList(), 0, 10
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(transferRepository.findBySourceCardId(eq(sourceCardId), any(Pageable.class)))
      .thenReturn(emptyPage);
    when(pageMapper.mapToPagedResponseDTO(eq(emptyPage), ArgumentMatchers.<Function<Transfer, TransferResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<TransferResponseDTO> result = transferService.getTransfers(filterDTO, userAuth);

    assertNotNull(result);
    assertEquals(0, result.totalElements());
    assertTrue(result.content().isEmpty());
  }

  @Test
  void getTransfers_NoCardsProvided_ThrowsException() {
    setupUserAuth();
    TransferFilterDTO filterDTO = new TransferFilterDTO(null, null, 0, 10);

    assertThrows(BadRequestException.class, () -> transferService.getTransfers(filterDTO, userAuth));
  }

  @Test
  void getTransfers_UnauthorizedUser_ThrowsException() {
    Authentication otherUserAuth = mock(Authentication.class);
    lenient().when(otherUserAuth.getName()).thenReturn("+98765432109");
    lenient().when(otherUserAuth.getAuthorities()).thenAnswer(invocation ->
      Collections.singletonList(new SimpleGrantedAuthority("USER")));

    TransferFilterDTO filterDTO = new TransferFilterDTO(sourceCardId, null, 0, 10);

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));

    assertThrows(UnauthorizedException.class, () -> transferService.getTransfers(filterDTO, otherUserAuth));
  }

  @Test
  void createTransfer_Success() {
    setupUserAuth();
    TransferCreateDTO createDTO = new TransferCreateDTO(
      "100.00",
      sourceCardId,
      destinationCardId
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));
    when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
    when(transferMapper.mapToResponseDTO(testTransfer, sourceCard, destinationCard))
      .thenReturn(testTransferResponse);

    TransferResponseDTO result = transferService.createTransfer(createDTO, userAuth);

    assertNotNull(result);
    assertEquals(testTransferResponse, result);
    verify(cardRepository).findById(sourceCardId);
    verify(cardRepository).findById(destinationCardId);
    verify(transferRepository).save(any(Transfer.class));
  }

  @Test
  void createTransfer_AsAdmin_Success() {
    setupAdminAuth();

    UserEntity sourceUser = new UserEntity();
    sourceUser.setId(UUID.randomUUID());
    sourceUser.setPhoneNumber("+11111111111");
    sourceCard.setOwner(sourceUser);

    UserEntity testUser = new UserEntity();
    testUser.setId(UUID.randomUUID());
    testUser.setPhoneNumber("+22222222222");
    destinationCard.setOwner(testUser);

    TransferCreateDTO createDTO = new TransferCreateDTO(
      "100.00",
      sourceCardId,
      destinationCardId
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));
    when(transferRepository.save(any(Transfer.class))).thenReturn(testTransfer);
    when(transferMapper.mapToResponseDTO(testTransfer, sourceCard, destinationCard))
      .thenReturn(testTransferResponse);

    TransferResponseDTO result = transferService.createTransfer(createDTO, adminAuth);

    assertNotNull(result);
    verify(cardRepository).findById(sourceCardId);
    verify(cardRepository).findById(destinationCardId);
    verify(transferRepository).save(any(Transfer.class));
  }

  @Test
  void createTransfer_InsufficientBalance_ThrowsException() {
    setupUserAuth();
    sourceCard.setBalance(BigDecimal.valueOf(50));

    TransferCreateDTO createDTO = new TransferCreateDTO(
      "100.00",
      sourceCardId,
      destinationCardId
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(cardRepository.findById(destinationCardId)).thenReturn(Optional.of(destinationCard));

    assertThrows(InsufficientBalanceException.class, () ->
      transferService.createTransfer(createDTO, userAuth));
    verify(transferRepository, never()).save(any());
  }

  @Test
  void createTransfer_SourceCardNotFound_ThrowsException() {
    setupUserAuth();
    TransferCreateDTO createDTO = new TransferCreateDTO(
      "100.00",
      sourceCardId,
      destinationCardId
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () ->
      transferService.createTransfer(createDTO, userAuth));
  }

  @Test
  void createTransfer_DestinationCardNotFound_ThrowsException() {
    setupUserAuth();
    TransferCreateDTO createDTO = new TransferCreateDTO(
      "100.00",
      sourceCardId,
      destinationCardId
    );

    when(cardRepository.findById(sourceCardId)).thenReturn(Optional.of(sourceCard));
    when(cardRepository.findById(destinationCardId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () ->
      transferService.createTransfer(createDTO, userAuth));
  }
}