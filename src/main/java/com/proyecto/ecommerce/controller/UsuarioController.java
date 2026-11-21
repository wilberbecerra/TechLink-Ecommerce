package com.proyecto.ecommerce.controller;

import com.proyecto.ecommerce.model.Usuario;
import com.proyecto.ecommerce.service.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

  private final IUsuarioService usuarios;
  private final IOrdenService ordenes;
  private final BCryptPasswordEncoder encoder;

  public UsuarioController(
    IUsuarioService u,
    IOrdenService o,
    BCryptPasswordEncoder e
  ) {
    usuarios = u;
    ordenes = o;
    encoder = e;
  }

  @GetMapping("/registro")
  public String register() {
    return "usuario/registro";
  }

  @GetMapping("/login")
  public String login() {
    return "usuario/login";
  }

  @PostMapping("/save")
  public String save(
    @RequestParam String nombre,
    @RequestParam String email,
    @RequestParam String direccion,
    @RequestParam String password,
    Model model
  ) {
    email = email.strip().toLowerCase(java.util.Locale.ROOT);
    if (
      nombre.isBlank() ||
      !email.matches("[^@\\s]+@[^@\\s]+[.][^@\\s]+") ||
      direccion.isBlank() ||
      password.length() < 8 ||
      usuarios.findByEmail(email).isPresent()
    ) {
      model.addAttribute(
        "errorRegistro",
        "Revisa tus datos. El correo debe ser único y la contraseña debe tener al menos 8 caracteres."
      );
      return "usuario/registro";
    }
    usuarios.save(
      new Usuario(
        null,
        nombre.strip(),
        email,
        email,
        direccion.strip(),
        null,
        "USER",
        encoder.encode(password)
      )
    );
    return "redirect:/usuario/login?registered";
  }

  @GetMapping("/acceder")
  public String access(Authentication a) {
    return usuarios
      .findByEmail(a.getName())
      .map(u ->
        "ADMIN".equals(u.getTipo()) ? "redirect:/administrador" : "redirect:/"
      )
      .orElse("redirect:/usuario/login");
  }

  @GetMapping("/compras")
  public String purchases(Model m, Authentication a) {
    m.addAttribute(
      "ordenes",
      ordenes.findByUsuario(usuarios.findByEmail(a.getName()).orElseThrow())
    );
    return "usuario/compras";
  }

  @GetMapping("/detalle/{id}")
  public String detail(@PathVariable Integer id, Model m, Authentication a) {
    var o = ordenes
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (
      !o.getUsuario().getEmail().equals(a.getName())
    ) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    m.addAttribute("orden", o);
    m.addAttribute("detalles", o.getDetalles());
    return "usuario/detallecompra";
  }

  @PostMapping("/cerrar")
  public String logout(
    HttpServletRequest req,
    HttpServletResponse res,
    Authentication a
  ) {
    new SecurityContextLogoutHandler().logout(req, res, a);
    return "redirect:/usuario/login?logout";
  }
}
