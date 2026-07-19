package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.enums.CardStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.PageMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.implementation.CardServiceImpl;
import com.example.bankcards.util.CardNumberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

  @Mock
  private CardRepository cardRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CardNumberService cardNumberService;

  @Mock
  private CardMapper cardMapper;

  @Mock
  private PageMapper pageMapper;

  @InjectMocks
  private CardServiceImpl cardService;

  private UserEntity testUser;
  private Card testCard;
  private CardResponseDTO testCardResponse;
  private Authentication userAuth;
  private Authentication adminAuth;
  private UUID cardId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    cardId = UUID.randomUUID();
    userId = UUID.randomUUID();

    testUser = new UserEntity();
    testUser.setId(userId);
    testUser.setPhoneNumber("+12345678901");
    testUser.setFullName("Test User");

    testCard = new Card();
    testCard.setId(cardId);
    testCard.setCardNumberHash("hashedCardNumber");
    testCard.setLastDigits("1234");
    testCard.setExpiryDate(LocalDate.of(2025, 12, 1));
    testCard.setStatus("ACTIVE");
    testCard.setBalance(BigDecimal.valueOf(1000));
    testCard.setOwner(testUser);

    testCardResponse = new CardResponseDTO(
      cardId,
      "****-****-****-1234",
      LocalDate.of(2025, 12, 1),
      "ACTIVE",
      BigDecimal.valueOf(1000),
      "Test User",
      "+12345678901"
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

  private PagedResponseDTO<CardResponseDTO> createPagedResponse(List<CardResponseDTO> content, int page, int size) {
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
  void getCards_AsAdmin_NoFilters_ReturnsAllCards() {
    setupAdminAuth();
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, "", "", "", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardRepository.findByStatus(eq("ACTIVE"), any(Pageable.class)))
      .thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(any(), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, adminAuth);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
    assertTrue(result.content().contains(testCardResponse));
    verify(cardRepository).findByStatus(eq("ACTIVE"), any(Pageable.class));
    verify(pageMapper).mapToPagedResponseDTO(any(), ArgumentMatchers.<Function<Card, CardResponseDTO>>any());
  }

  @Test
  void getCards_AsAdmin_WithOwnerFullName_ReturnsFilteredCards() {
    setupAdminAuth();
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, "", "Test", "", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardRepository.findByOwnerFullNameContainingIgnoreCaseAndStatus(
      eq("Test"), eq("ACTIVE"), any(Pageable.class)))
      .thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(eq(cardPage), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, adminAuth);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    verify(cardRepository).findByOwnerFullNameContainingIgnoreCaseAndStatus(
      eq("Test"), eq("ACTIVE"), any(Pageable.class));
  }

  @Test
  void getCards_AsAdmin_WithOwnerFullNameAndLastDigits_ReturnsFilteredCards() {
    setupAdminAuth();
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, "", "Test", "1234", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardRepository.findByOwnerFullNameContainingIgnoreCaseAndLastDigitsContainingAndStatus(
      eq("Test"), eq("1234"), eq("ACTIVE"), any(Pageable.class)))
      .thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(eq(cardPage), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, adminAuth);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    verify(cardRepository).findByOwnerFullNameContainingIgnoreCaseAndLastDigitsContainingAndStatus(
      eq("Test"), eq("1234"), eq("ACTIVE"), any(Pageable.class));
  }

  @Test
  void getCards_AsAdmin_WithCardNumber_ReturnsFilteredCards() {
    setupAdminAuth();
    String cardNumber = "1234567890123456";
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, cardNumber, "", "", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardNumberService.hashCardNumber(cardNumber)).thenReturn("hashedCardNumber");
    when(cardRepository.findByCardNumberHash("hashedCardNumber", PageRequest.of(0, 1))).thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(eq(cardPage), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, adminAuth);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    verify(cardNumberService).hashCardNumber(cardNumber);
    verify(cardRepository).findByCardNumberHash("hashedCardNumber", PageRequest.of(0, 1));
  }

  @Test
  void getCards_AsAdmin_WithLastDigits_ReturnsFilteredCards() {
    setupAdminAuth();
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.BLOCKED, "", "", "1234", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardRepository.findByLastDigitsContainingAndStatus(
      eq("1234"), eq("BLOCKED"), any(Pageable.class)))
      .thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(eq(cardPage), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, adminAuth);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    verify(cardRepository).findByLastDigitsContainingAndStatus(
      eq("1234"), eq("BLOCKED"), any(Pageable.class));
  }

  @Test
  void getCards_AsUser_NoFilters_ReturnsUserCards() {
    setupUserAuth();
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, "", "", "", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardRepository.findByOwnerPhoneNumberAndStatus(
      eq("+12345678901"), eq("ACTIVE"), any(Pageable.class)))
      .thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(eq(cardPage), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, userAuth);

    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
    verify(cardRepository).findByOwnerPhoneNumberAndStatus(
      eq("+12345678901"), eq("ACTIVE"), any(Pageable.class));
  }

  @Test
  void getCards_AsUser_WithLastDigits_ReturnsFilteredCards() {
    setupUserAuth();
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, "", "", "1234", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardRepository.findByOwnerPhoneNumberAndLastDigitsContainingAndStatus(
      eq("+12345678901"), eq("1234"), eq("ACTIVE"), any(Pageable.class)))
      .thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(eq(cardPage), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, userAuth);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    verify(cardRepository).findByOwnerPhoneNumberAndLastDigitsContainingAndStatus(
      eq("+12345678901"), eq("1234"), eq("ACTIVE"), any(Pageable.class));
  }

  @Test
  void getCards_AsUser_WithCardNumber_ReturnsFilteredCards() {
    setupUserAuth();
    String cardNumber = "1234567890123456";
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, cardNumber, "", "", 0, 10
    );

    Page<Card> cardPage = new PageImpl<>(List.of(testCard));
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      List.of(testCardResponse), 0, 10
    );

    when(cardNumberService.hashCardNumber(cardNumber)).thenReturn("hashedCardNumber");
    when(cardRepository.findByCardNumberHashAndOwnerPhoneNumber(
      eq("hashedCardNumber"), eq("+12345678901"), eq(PageRequest.of(0, 1))))
      .thenReturn(cardPage);
    when(pageMapper.mapToPagedResponseDTO(eq(cardPage), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, userAuth);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    verify(cardNumberService).hashCardNumber(cardNumber);
    verify(cardRepository).findByCardNumberHashAndOwnerPhoneNumber(
      eq("hashedCardNumber"), eq("+12345678901"), eq(PageRequest.of(0, 1)));
  }

  @Test
  void getCards_EmptyResult_ReturnsEmptyPage() {
    setupAdminAuth();
    CardFilterDTO filterDTO = new CardFilterDTO(
      CardStatus.ACTIVE, "", "", "", 0, 10
    );

    Page<Card> emptyPage = new PageImpl<>(Collections.emptyList());
    PagedResponseDTO<CardResponseDTO> expectedResponse = createPagedResponse(
      Collections.emptyList(), 0, 10
    );

    when(cardRepository.findByStatus(eq("ACTIVE"), any(Pageable.class)))
      .thenReturn(emptyPage);
    when(pageMapper.mapToPagedResponseDTO(any(), ArgumentMatchers.<Function<Card, CardResponseDTO>>any()))
      .thenReturn(expectedResponse);

    PagedResponseDTO<CardResponseDTO> result = cardService.getCards(filterDTO, adminAuth);

    assertNotNull(result);
    assertEquals(0, result.totalElements());
    assertTrue(result.content().isEmpty());
  }

  @Test
  void getCardById_Success() {
    setupUserAuth();
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(cardMapper.mapToResponseDTO(testCard)).thenReturn(testCardResponse);

    CardResponseDTO result = cardService.getCardById(cardId, userAuth);

    assertNotNull(result);
    assertEquals(testCardResponse.id(), result.id());
    verify(cardRepository).findById(cardId);
  }

  @Test
  void getCardById_NotFound_ThrowsException() {
    setupUserAuth();
    when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> cardService.getCardById(cardId, userAuth));
    verify(cardRepository).findById(cardId);
  }

  @Test
  void getCardById_UnauthorizedUser_ThrowsException() {
    Authentication otherUserAuth = mock(Authentication.class);
    lenient().when(otherUserAuth.getName()).thenReturn("+98765432109");
    lenient().when(otherUserAuth.getAuthorities()).thenAnswer(invocation ->
      Collections.singletonList(new SimpleGrantedAuthority("USER")));

    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

    assertThrows(UnauthorizedException.class, () -> cardService.getCardById(cardId, otherUserAuth));
    verify(cardRepository).findById(cardId);
  }

  @Test
  void createCard_Success() {
    String cardNumber = "1234567890123456";
    String hashedCardNumber = "hashedCardNumber";

    CardCreateDTO createDTO = new CardCreateDTO(
      cardNumber,
      LocalDate.of(2025, 12, 1),
      userId
    );

    when(cardNumberService.hashCardNumber(cardNumber)).thenReturn(hashedCardNumber);
    when(cardRepository.existsByCardNumberHash(hashedCardNumber)).thenReturn(false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(cardNumberService.extractLastDigits(cardNumber)).thenReturn("3456");
    when(cardRepository.save(any(Card.class))).thenReturn(testCard);
    when(cardMapper.mapToResponseDTO(testCard)).thenReturn(testCardResponse);

    CardResponseDTO result = cardService.createCard(createDTO);

    assertNotNull(result);
    assertEquals(testCardResponse, result);

    verify(cardNumberService, times(2)).hashCardNumber(cardNumber);
    verify(cardRepository).existsByCardNumberHash(hashedCardNumber);
    verify(userRepository).findById(userId);
    verify(cardNumberService).extractLastDigits(cardNumber);
    verify(cardRepository).save(any(Card.class));
    verify(cardMapper).mapToResponseDTO(testCard);
  }

  @Test
  void createCard_CardNumberAlreadyExists_ThrowsException() {
    String cardNumber = "1234567890123456";
    String hashedCardNumber = "hashed_" + cardNumber;

    CardCreateDTO createDTO = new CardCreateDTO(
      cardNumber,
      LocalDate.of(2025, 12, 1),
      userId
    );

    when(cardNumberService.hashCardNumber(cardNumber))
      .thenReturn(hashedCardNumber);
    when(cardRepository.existsByCardNumberHash(hashedCardNumber))
      .thenReturn(true);

    BadRequestException exception = assertThrows(
      BadRequestException.class,
      () -> cardService.createCard(createDTO)
    );

    assertEquals("Card with card number " + cardNumber + " already exists", exception.getMessage());

    verify(cardNumberService).hashCardNumber(cardNumber);
    verify(cardRepository).existsByCardNumberHash(hashedCardNumber);

    verify(userRepository, never()).findById(any(UUID.class));
    verify(cardRepository, never()).save(any(Card.class));
    verify(cardNumberService, never()).extractLastDigits(anyString());
  }

  @Test
  void createCard_UserNotFound_ThrowsException() {
    UUID nonExistentUserId = UUID.randomUUID();
    String cardNumber = "1234567890123456";
    String hashedCardNumber = "hashed_" + cardNumber;
    LocalDate expiryDate = LocalDate.of(2025, 12, 1);

    CardCreateDTO cardCreateDTO = new CardCreateDTO(
      cardNumber,
      expiryDate,
      nonExistentUserId
    );

    when(cardNumberService.hashCardNumber(cardNumber))
      .thenReturn(hashedCardNumber);
    when(cardRepository.existsByCardNumberHash(hashedCardNumber))
      .thenReturn(false);
    when(userRepository.findById(nonExistentUserId))
      .thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(
      ResourceNotFoundException.class,
      () -> cardService.createCard(cardCreateDTO)
    );

    assertEquals("User not found with id: " + nonExistentUserId, exception.getMessage());

    verify(cardNumberService).hashCardNumber(cardNumber);
    verify(cardRepository).existsByCardNumberHash(hashedCardNumber);
    verify(userRepository).findById(nonExistentUserId);

    verify(cardRepository, never()).save(any(Card.class));
  }

  @Test
  void updateCard_Success() {
    CardUpdateDTO updateDTO = new CardUpdateDTO(CardStatus.BLOCKED, "500.00");

    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(cardRepository.save(any(Card.class))).thenReturn(testCard);
    when(cardMapper.mapToResponseDTO(testCard)).thenReturn(testCardResponse);

    CardResponseDTO result = cardService.updateCard(cardId, updateDTO);

    assertNotNull(result);
    verify(cardRepository).findById(cardId);
    verify(cardRepository).save(any(Card.class));
  }

  @Test
  void deleteCard_Success() {
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    doNothing().when(cardRepository).delete(testCard);

    UUID result = cardService.deleteCard(cardId);

    assertEquals(cardId, result);
    verify(cardRepository).findById(cardId);
    verify(cardRepository).delete(testCard);
  }

  @Test
  void deleteCard_NotFound_ThrowsException() {
    when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> cardService.deleteCard(cardId));
    verify(cardRepository).findById(cardId);
    verify(cardRepository, never()).delete(any());
  }

  @Test
  void createCardBlockRequest_Success() {
    setupUserAuth();
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(cardRepository.save(any(Card.class))).thenReturn(testCard);
    when(cardMapper.mapToResponseDTO(testCard)).thenReturn(testCardResponse);

    CardResponseDTO result = cardService.createCardBlockRequest(cardId, userAuth);

    assertNotNull(result);
    assertEquals("BLOCK_REQUEST", testCard.getStatus());
    verify(cardRepository).findById(cardId);
    verify(cardRepository).save(any(Card.class));
  }
}