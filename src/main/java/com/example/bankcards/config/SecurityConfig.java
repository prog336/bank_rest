package com.example.bankcards.config;

import com.example.bankcards.security.JWTAuthEntryPoint;
import com.example.bankcards.security.JWTAuthFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Autowired
  private UserDetailsServiceImpl userDetailsService;
  @Autowired
  private JWTAuthEntryPoint authEntryPoint;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    return http.csrf(AbstractHttpConfigurer::disable)
      .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(authEntryPoint))
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
          .requestMatchers("/api/auth/**").permitAll()
          .requestMatchers("/swagger-ui/**").permitAll()
          .requestMatchers("/v3/**").permitAll()
          .requestMatchers("/openapi.yaml/**").permitAll()
          .requestMatchers(HttpMethod.GET, "/api/cards").hasAuthority("USER")
          .requestMatchers(HttpMethod.GET, "/api/cards/{cardId}").hasAuthority("USER")
          .requestMatchers("/api/cards/{cardId}/block-request").hasAuthority("USER")
          .requestMatchers("/api/transfers/**").hasAuthority("USER")
          .requestMatchers("/api/**").hasAuthority("ADMIN"))
      .authenticationProvider(authenticationProvider())
      .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
      .build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(){
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());

    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JWTAuthFilter jwtAuthFilter(){
    return new JWTAuthFilter();
  }
}
