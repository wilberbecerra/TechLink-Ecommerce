package com.proyecto.ecommerce.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

  private final IUsuarioService usuarios;

  public UserDetailServiceImpl(IUsuarioService usuarios) {
    this.usuarios = usuarios;
  }

  @Override
  public UserDetails loadUserByUsername(
    String email
  ) throws UsernameNotFoundException {
    var u = usuarios
      .findByEmail(email)
      .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario"));
    return User.withUsername(u.getEmail())
      .password(u.getPassword())
      .roles(u.getTipo())
      .build();
  }
}
