package com.example.bankcards.config;

import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardNumberService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Testcontainers
public class TestConfig {

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:18")
    .withDatabaseName("cardManager")
    .withUsername("postgres")
    .withPassword("postgres");

  static {
    postgreSQLContainer.start();
    System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
    System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
    System.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    System.setProperty("spring.liquibase.enabled", "false");
    System.setProperty("spring.dotenv.enabled", "false");
    System.setProperty("jwt.secret", "testsecretkeyforjunittestspurposestestsecretkey");
    System.setProperty("jwt.expiration", "3600000");
    System.setProperty("springdoc.api-docs.enabled", "false");
    System.setProperty("springdoc.swagger-ui.enabled", "false");
  }

  @Bean
  @Primary
  public AuthService authService() {
    return mock(AuthService.class);
  }

  @Bean
  @Primary
  public CardService cardService() {
    return mock(CardService.class);
  }

  @Bean
  @Primary
  public TransferService transferService() {
    return mock(TransferService.class);
  }

  @Bean
  @Primary
  public UserService userService() {
    return mock(UserService.class);
  }

  @Bean
  @Primary
  public CardNumberService cardNumberService() {
    return mock(CardNumberService.class);
  }
}