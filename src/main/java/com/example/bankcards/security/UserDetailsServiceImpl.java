package com.example.bankcards.security;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByPhoneNumber(username).orElseThrow(()->new UsernameNotFoundException(username + "not found"));
    List<GrantedAuthority> rolesToAuthorities = user.getRoles().stream().map
      (role -> new SimpleGrantedAuthority(role.getName()))
      .collect(Collectors.toList());

    return new User(user.getPhoneNumber(), user.getPassword(), rolesToAuthorities);
  }
}
