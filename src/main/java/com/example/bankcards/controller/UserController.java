package com.example.bankcards.controller;

import com.example.bankcards.dto.UserFilterDTO;
import com.example.bankcards.dto.UserRequestDTO;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/users")
@AllArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping
  public ResponseEntity<?> getUsers(@Valid UserFilterDTO userFilterDTO){
    return ResponseEntity.ok(userService.getUsers(userFilterDTO));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<?> getUserById(@PathVariable UUID userId){
    return ResponseEntity.ok(userService.getUserById(userId));
  }

  @PostMapping
  public ResponseEntity<?> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO){
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDTO));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<?> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserRequestDTO userRequestDTO){
    return ResponseEntity.ok(userService.updateUser(userId, userRequestDTO));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<?> deleteUser(@PathVariable UUID userId){
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(userService.deleteUser(userId));
  }
}