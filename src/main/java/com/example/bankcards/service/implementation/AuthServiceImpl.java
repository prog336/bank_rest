package com.example.bankcards.service.implementation;

import com.example.bankcards.dto.LoginDTO;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JWTGenerator;
import com.example.bankcards.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JWTGenerator jwtGenerator;

  @Override
  public String loginUser(LoginDTO loginDTO){
    UserEntity user = userRepository.findByPhoneNumber(loginDTO.phoneNumber())
      .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + loginDTO.phoneNumber()));

    if (!passwordEncoder.matches(loginDTO.password(), user.getPassword())){
      throw new UnauthorizedException("Wrong phone number or password");
    }

    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.phoneNumber(), loginDTO.password()));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    return jwtGenerator.generateToken(authentication);
  }
}