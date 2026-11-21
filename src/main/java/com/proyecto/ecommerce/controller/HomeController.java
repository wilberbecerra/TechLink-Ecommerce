package com.proyecto.ecommerce.controller;

import com.proyecto.ecommerce.model.*;
import com.proyecto.ecommerce.repository.*;
import com.proyecto.ecommerce.service.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class HomeController {

  private final ProductoService productos;
  private final IUsuarioService usuarios;
  private final IOrdenService ordenes;
  private final IDetalleOrdenService detallesService;
  private final IProductoRepository productoRepository;

  public HomeController(
    ProductoService p,
    IUsuarioService u,
    IOrdenService o,
    IDetalleOrdenService d,
    IProductoRepository pr
  ) {
    productos = p;
    usuarios = u;
    ordenes = o;
    detallesService = d;
    productoRepository = pr;
  }

  @SuppressWarnings("unchecked")
  private List<DetalleOrden> cart(HttpSession s) {
    if (s.getAttribute("cart") == null) s.setAttribute(
      "cart",
      new ArrayList<DetalleOrden>()
    );
    return (List<DetalleOrden>) s.getAttribute("cart");
  }

  private void cartModel(Model model, HttpSession s) {
    var items = cart(s);
    var order = new Orden();
    order.setTotal(items.stream().mapToDouble(DetalleOrden::getTotal).sum());
    model.addAttribute("cart", items);
    model.addAttribute("orden", order);
  }

  private Producto producto(Integer id) {
    return productos
      .get(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private Usuario usuario(Authentication a) {
    return usuarios
      .findByEmail(a.getName())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  private Orden ownedOrder(Integer id, Authentication a) {
    var o = ordenes
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    var u = usuario(a);
    if (
      !"ADMIN".equals(u.getTipo()) && !o.getUsuario().getId().equals(u.getId())
    ) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return o;
  }

  @GetMapping("/")
  public String home(
    @RequestParam(defaultValue = "") String nombre,
    @RequestParam(defaultValue = "") String categoria,
    Model model
  ) {
    var all = productos.findAll();
    model.addAttribute(
      "productos",
      all
        .stream()
        .filter(p ->
          p
            .getNombre()
            .toLowerCase(Locale.ROOT)
            .contains(nombre.toLowerCase(Locale.ROOT))
        )
        .filter(p -> categoria.isBlank() || p.getCategoria().equals(categoria))
        .toList()
    );
    model.addAttribute("nombre", nombre);
    model.addAttribute("categoria", categoria);
    model.addAttribute(
      "destacado",
      all
        .stream()
        .filter(p -> p.getNombre().startsWith("Sony"))
        .findFirst()
        .orElse(all.isEmpty() ? null : all.get(0))
    );
    return "usuario/home";
  }

  @PostMapping("/search")
  public String search(
    @RequestParam String nombre,
    org.springframework.web.servlet.mvc.support.RedirectAttributes redirect
  ) {
    redirect.addAttribute("nombre", nombre);
    return "redirect:/";
  }

  @GetMapping("/productohome/{id}")
  public String product(@PathVariable Integer id, Model m) {
    m.addAttribute("producto", producto(id));
    return "usuario/productohome";
  }

  @PostMapping("/cart")
  public String addCart(
    @RequestParam Integer id,
    @RequestParam Integer cantidad,
    HttpSession s
  ) {
    var p = producto(id);
    var list = cart(s);
    var existing = list
      .stream()
      .filter(d -> d.getProducto().getId().equals(id))
      .findFirst();
    int qty =
      cantidad +
      (existing.isPresent() ? (int) existing.get().getCantidad() : 0);
    if (
      cantidad < 1 || qty < 1 || qty > p.getCantidad()
    ) throw new ResponseStatusException(
      HttpStatus.BAD_REQUEST,
      "Cantidad no disponible"
    );
    var d = existing.orElseGet(() -> {
      var n = new DetalleOrden();
      n.setProducto(p);
      list.add(n);
      return n;
    });
    d.setNombre(p.getNombre());
    d.setCantidad(qty);
    d.setPrecio(p.getPrecio());
    d.setTotal(qty * p.getPrecio());
    return "redirect:/getCart";
  }

  @PostMapping("/delete/cart/{id}")
  public String remove(@PathVariable Integer id, HttpSession s) {
    cart(s).removeIf(d -> d.getProducto().getId().equals(id));
    return "redirect:/getCart";
  }

  @GetMapping("/getCart")
  public String getCart(Model m, HttpSession s) {
    cartModel(m, s);
    return "usuario/carrito";
  }

  @GetMapping("/order")
  public String order(Model m, HttpSession s, Authentication a) {
    if (cart(s).isEmpty()) return "redirect:/getCart";
    cartModel(m, s);
    m.addAttribute("usuario", usuario(a));
    return "usuario/resumenorden";
  }

  @PostMapping("/saveOrder")
  @Transactional
  public String saveOrder(HttpSession s, Authentication a) {
    var items = cart(s);
    if (items.isEmpty()) return "redirect:/getCart";
    // Reload and lock stock: totals always come from current server-side prices.
    var locked = new HashMap<Integer, Producto>();
    items
      .stream()
      .map(d -> d.getProducto().getId())
      .sorted()
      .forEach(id ->
        locked.put(id, productoRepository.findForCheckout(id).orElseThrow())
      );
    double total = 0;
    for (var d : items) {
      var p = locked.get(d.getProducto().getId());
      if (
        d.getCantidad() < 1 || d.getCantidad() > p.getCantidad()
      ) throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "El stock cambió. Revisa tu carrito."
      );
      total += p.getPrecio() * d.getCantidad();
    }
    var o = new Orden();
    o.setUsuario(usuario(a));
    o.setFechaCreacion(new Date());
    o.setTotal(total);
    o.setNumero(ordenes.generarNumeroOrden());
    ordenes.save(o);
    for (var d : items) {
      var p = locked.get(d.getProducto().getId());
      var line = new DetalleOrden(
        null,
        p.getNombre(),
        d.getCantidad(),
        p.getPrecio(),
        p.getPrecio() * d.getCantidad()
      );
      line.setOrden(o);
      line.setProducto(p);
      detallesService.save(line);
      p.setCantidad(p.getCantidad() - (int) d.getCantidad());
      productos.save(p);
    }
    items.clear();
    return "redirect:/compraExitosa/" + o.getId();
  }

  @GetMapping("/compraExitosa/{id}")
  public String success(@PathVariable Integer id, Model m, Authentication a) {
    var o = ownedOrder(id, a);
    m.addAttribute("orden", o);
    m.addAttribute("idOrden", id);
    return "usuario/compra_exitosa";
  }

  @GetMapping("/pdf/{id}")
  public void pdf(
    @PathVariable Integer id,
    Authentication a,
    HttpServletResponse response
  ) throws IOException, JRException {
    var o = ownedOrder(id, a);
    try (
      InputStream stream = getClass().getResourceAsStream(
        "/reports/boleta_factura.jrxml"
      )
    ) {
      if (stream == null) throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR
      );
      var report = JasperCompileManager.compileReport(stream);
      Map<String, Object> params = new HashMap<>();
      params.put("nombreCliente", o.getUsuario().getNombre());
      params.put("emailCliente", o.getUsuario().getEmail());
      params.put("total", o.getTotal());
      var print = JasperFillManager.fillReport(
        report,
        params,
        new JRBeanCollectionDataSource(o.getDetalles())
      );
      response.setContentType("application/pdf");
      response.setHeader(
        "Content-Disposition",
        "inline; filename=Comprobante_Orden_" + o.getNumero() + ".pdf"
      );
      JasperExportManager.exportReportToPdfStream(
        print,
        response.getOutputStream()
      );
    }
  }
}
