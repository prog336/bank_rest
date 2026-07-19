package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginDTO;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@AllArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO){
    return ResponseEntity.ok(authService.loginUser(loginDTO));
  }
}