package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTGenerator {
  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private Long expirationTime;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String generateToken(Authentication authentication){
    String username = authentication.getName();
    Date expireDate = new Date(new Date().getTime() + expirationTime);

    return Jwts.builder()
      .subject(username)
      .issuedAt(new Date())
      .expiration(expireDate)
      .signWith(getSigningKey(), Jwts.SIG.HS512)
      .compact();
  }

  public String getUserIdFromJWT(String token){
    Claims claims = Jwts.parser()
      .verifyWith(getSigningKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();

    return claims.getSubject();
  }

  public boolean validateToken(String token){
    try {
      Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token);

      return true;
    }catch (Exception ex){
      throw new AuthenticationCredentialsNotFoundException("JWT expired or incorrect");
    }
  }
}
