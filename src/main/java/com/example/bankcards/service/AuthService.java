package com.example.bankcards.service;

import com.example.bankcards.dto.LoginDTO;

public interface AuthService {
  String loginUser(LoginDTO loginDTO);
}
