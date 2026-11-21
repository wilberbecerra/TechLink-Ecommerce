package com.proyecto.ecommerce.controller;

import com.proyecto.ecommerce.model.Producto;
import com.proyecto.ecommerce.repository.IDetalleOrdenRepository;
import com.proyecto.ecommerce.service.*;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/productos")
public class ProductoController {

  private final ProductoService productos;
  private final IUsuarioService usuarios;
  private final UploadFileService upload;
  private final IDetalleOrdenRepository detalles;

  public ProductoController(
    ProductoService p,
    IUsuarioService u,
    UploadFileService up,
    IDetalleOrdenRepository d
  ) {
    productos = p;
    usuarios = u;
    upload = up;
    detalles = d;
  }

  private Producto find(Integer id) {
    return productos
      .get(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private void validate(Producto p) {
    if (
      p.getNombre() == null ||
      p.getNombre().isBlank() ||
      p.getNombre().length() > 255 ||
      p.getDescripcion() == null ||
      p.getDescripcion().isBlank() ||
      p.getDescripcion().length() > 2000 ||
      p.getCantidad() < 0 ||
      !Double.isFinite(p.getPrecio()) ||
      p.getPrecio() < 0
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Revisa los datos del producto."
    );
  }

  @GetMapping("")
  public String show(Model m) {
    m.addAttribute("productos", productos.findAll());
    return "productos/show";
  }

  @GetMapping("/create")
  public String create() {
    return "productos/create";
  }

  @PostMapping("/save")
  public String save(
    @ModelAttribute Producto p,
    @RequestParam("img") MultipartFile file,
    Authentication a
  ) throws IOException {
    validate(p);
    p.setId(null);
    p.setUsuario(usuarios.findByEmail(a.getName()).orElseThrow());
    p.setImagen(upload.saveImage(file));
    productos.save(p);
    return "redirect:/productos";
  }

  @GetMapping("/edit/{id}")
  public String edit(@PathVariable Integer id, Model m) {
    m.addAttribute("producto", find(id));
    return "productos/edit";
  }

  @PostMapping("/update")
  public String update(
    @ModelAttribute Producto p,
    @RequestParam("img") MultipartFile file
  ) throws IOException {
    validate(p);
    var old = find(p.getId());
    p.setUsuario(old.getUsuario());
    p.setImagen(file.isEmpty() ? old.getImagen() : upload.saveImage(file));
    productos.update(p);
    if (!file.isEmpty()) upload.deleteImage(old.getImagen());
    return "redirect:/productos";
  }

  @PostMapping("/delete/{id}")
  public String delete(@PathVariable Integer id) {
    var p = find(id);
    if (detalles.existsByProducto_Id(id)) return "redirect:/productos?inUse";
    productos.delete(id);
    upload.deleteImage(p.getImagen());
    return "redirect:/productos";
  }
}
