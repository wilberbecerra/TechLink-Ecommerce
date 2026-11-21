package com.proyecto.ecommerce.config;

import com.proyecto.ecommerce.service.IUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ViewModelAdvice {

  private final IUsuarioService usuarios;

  public ViewModelAdvice(IUsuarioService usuarios) {
    this.usuarios = usuarios;
  }

  @ModelAttribute
  public void shared(
    Model model,
    HttpServletRequest request,
    Authentication authentication
  ) {
    model.addAttribute("appPath", request.getRequestURI());
    if (authentication != null) usuarios
      .findByEmail(authentication.getName())
      .ifPresent(u -> {
        model.addAttribute("currentUser", u);
        request.getSession().setAttribute("idusuario", u.getId());
        model.addAttribute("isAdmin", "ADMIN".equals(u.getTipo()));
      });
  }
}
