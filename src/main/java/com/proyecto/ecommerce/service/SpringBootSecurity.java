package com.proyecto.ecommerce.service;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringBootSecurity {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers("/administrador/**", "/productos/**")
          .hasRole("ADMIN")
          .requestMatchers(
            "/usuario/acceder",
            "/usuario/compras",
            "/usuario/detalle/**",
            "/order",
            "/saveOrder",
            "/compraExitosa/**",
            "/pdf/**"
          )
          .authenticated()
          .anyRequest()
          .permitAll()
      )
      .formLogin(form ->
        form
          .loginPage("/usuario/login")
          .defaultSuccessUrl("/usuario/acceder", true)
          .permitAll()
      )
      .logout(logout ->
        logout.logoutSuccessUrl("/usuario/login?logout").permitAll()
      )
      .build();
  }
}
