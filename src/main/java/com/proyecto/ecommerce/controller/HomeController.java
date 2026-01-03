package com.proyecto.ecommerce.controller;

import com.proyecto.ecommerce.model.DetalleOrden;
import com.proyecto.ecommerce.model.Orden;
import com.proyecto.ecommerce.model.Producto;
import com.proyecto.ecommerce.model.Usuario;
import com.proyecto.ecommerce.service.IDetalleOrdenService;
import com.proyecto.ecommerce.service.IOrdenService;
import com.proyecto.ecommerce.service.IUsuarioService;
import com.proyecto.ecommerce.service.ProductoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Imports para JasperReports
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class HomeController {

    private final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private ProductoService productoService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IOrdenService ordenService;

    @Autowired
    private IDetalleOrdenService detalleOrdenService;

    // NOTA IMPORTANTE: Estas variables globales 'detalles' y 'orden'
    // funcionan para un solo usuario a la vez (proyecto académico).
    // En producción, esto debería manejarse por Sesión para evitar cruce de datos.
    List<DetalleOrden> detalles = new ArrayList<DetalleOrden>();
    Orden orden = new Orden();

    @GetMapping("")
    public String home(Model model, HttpSession session) {
        log.info("Sesion del usuario: {}", session.getAttribute("idusuario"));
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("sesion", session.getAttribute("idusuario"));
        return "usuario/home";
    }

    @GetMapping("productohome/{id}")
    public String productoHome(@PathVariable Integer id, Model model) {
        log.info("Id producto enviado como parametro {}", id);
        Producto producto = new Producto();
        Optional<Producto> productoOptional = productoService.get(id);
        producto = productoOptional.get();

        model.addAttribute("producto", producto);
        return "usuario/productohome";
    }

    @PostMapping("/cart")
    public String addCart(@RequestParam Integer id, @RequestParam Integer cantidad, Model model) {
        DetalleOrden detalleOrden = new DetalleOrden();
        Producto producto = new Producto();
        double sumaTotal = 0;

        Optional<Producto> optionalProducto = productoService.get(id);
        log.info("Producto agregado: {}", optionalProducto.get());
        log.info("Cantidad: {}", cantidad);
        producto = optionalProducto.get();

        detalleOrden.setCantidad(cantidad);
        detalleOrden.setPrecio(producto.getPrecio());
        detalleOrden.setNombre(producto.getNombre());
        detalleOrden.setTotal(producto.getPrecio() * cantidad);
        detalleOrden.setProducto(producto);

        // Validar que el producto no se añada dos veces
        Integer idProducto = producto.getId();
        boolean ingresado = detalles.stream().anyMatch(p -> p.getProducto().getId() == idProducto);
        if (!ingresado) {
            detalles.add(detalleOrden);
        }

        sumaTotal = detalles.stream().mapToDouble(dt -> dt.getTotal()).sum();

        orden.setTotal(sumaTotal);
        model.addAttribute("cart", detalles);
        model.addAttribute("orden", orden);

        return "usuario/carrito";
    }

    // Eliminar un producto del carrito
    @GetMapping("/delete/cart/{id}")
    public String deleteProductoCart(@PathVariable Integer id, Model model) {
        // Lista nueva de productos
        List<DetalleOrden> ordenesNueva = new ArrayList<DetalleOrden>();

        for (DetalleOrden detalleOrden : detalles) {
            if (detalleOrden.getProducto().getId() != id) {
                ordenesNueva.add(detalleOrden);
            }
        }

        // Colocar la nueva lista con los productos restantes
        detalles = ordenesNueva;

        double sumaTotal = 0;
        sumaTotal = detalles.stream().mapToDouble(dt -> dt.getTotal()).sum();
        orden.setTotal(sumaTotal);
        model.addAttribute("cart", detalles);
        model.addAttribute("orden", orden);

        return "usuario/carrito";
    }

    @GetMapping("/getCart")
    public String getCart(Model model, HttpSession session) {
        model.addAttribute("cart", detalles);
        model.addAttribute("orden", orden);
        model.addAttribute("sesion", session.getAttribute("idusuario"));
        return "/usuario/carrito";
    }

    @GetMapping("/order")
    public String order(Model model, HttpSession session) {
        // 1. Verificar si el usuario está logueado
        Object idUsuario = session.getAttribute("idusuario");

        if (idUsuario == null) {
            // Si es nulo, redirigir al registro/login
            return "redirect:/usuario/login";
        }

        // 2. Si existe sesión, continuar normalmente
        Usuario usuario = usuarioService.findById(Integer.parseInt(idUsuario.toString())).get();

        model.addAttribute("cart", detalles);
        model.addAttribute("orden", orden);
        model.addAttribute("usuario", usuario);

        return "usuario/resumenorden";
    }

    // Guardar la orden
    @GetMapping("/saveOrder")
    public String saveOrder(HttpSession session) {
        // También protegemos este método por si intentan guardar sin sesión
        Object idUsuario = session.getAttribute("idusuario");
        if (idUsuario == null) {
            return "redirect:/usuario/registro";
        }

        Date fechaCreacion = new Date();
        orden.setFechaCreacion(fechaCreacion);
        orden.setNumero(ordenService.generarNumeroOrden());

        // Usuario
        Usuario usuario = usuarioService.findById(Integer.parseInt(idUsuario.toString())).get();
        orden.setUsuario(usuario);

        // Guardar la orden y OBTENER el objeto guardado (con el ID generado)
        Orden ordenGuardada = ordenService.save(orden);

        // Guardar los detalles
        for (DetalleOrden dt : detalles) {
            dt.setOrden(ordenGuardada);
            detalleOrdenService.save(dt);
        }

        // Limpiar la lista y la orden local
        orden = new Orden();
        detalles.clear();

        // Redirigir a la vista de éxito pasando el ID de la orden para el PDF
        return "redirect:/compraExitosa/" + ordenGuardada.getId();
    }

    @PostMapping("/search")
    public String searchProduct(@RequestParam String nombre, Model model) {
        log.info("Nombre del producto: {}", nombre);
        List<Producto> productos = productoService.findAll().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
        model.addAttribute("productos", productos);
        return "usuario/home";
    }

    // ==========================================
    // SECCIÓN DE JASPER REPORTS (PDF)
    // ==========================================

    @GetMapping("/compraExitosa/{id}")
    public String compraExitosa(@PathVariable Integer id, Model model) {
        // Pasamos el ID a la vista para que el botón "Descargar PDF" sepa qué descargar
        model.addAttribute("idOrden", id);
        return "usuario/compra_exitosa";
    }

    @GetMapping("/pdf/{id}")
    public void generarPdf(@PathVariable Integer id, HttpServletResponse response) throws IOException, JRException {

        // 1. Buscar la orden en la Base de Datos
        Optional<Orden> ordenOptional = ordenService.findById(id);
        if (ordenOptional.isEmpty()) {
            // Si no existe, redirigir o no hacer nada (aquí podrías manejar un error)
            return;
        }
        Orden ordenDb = ordenOptional.get();

        // 2. Cargar el archivo .jrxml desde resources/reports
        // Asegúrate que la ruta src/main/resources/reports/boleta_factura.jrxml exista
        InputStream reportStream = getClass().getResourceAsStream("/reports/boleta_factura.jrxml");

        if (reportStream == null) {
            log.error("Archivo no encontrado: /reports/boleta_factura.jrxml");
            return;
        }

        // 3. Compilar el reporte
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // 4. Parámetros (Datos del Cliente y Totales)
        Map<String, Object> params = new HashMap<>();
        params.put("nombreCliente", ordenDb.getUsuario().getNombre());
        params.put("emailCliente", ordenDb.getUsuario().getEmail());
        params.put("total", ordenDb.getTotal());

        // 5. Datasource (La lista de productos)
        // AQUI ESTABA LA CLAVE: 'getDetalles()' debe coincidir con tu entidad Orden
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ordenDb.getDetalles());

        // 6. Llenar el reporte con datos
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

        // 7. Configurar la respuesta del navegador para descargar PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Comprobante_Orden_" + ordenDb.getNumero() + ".pdf");

        // 8. Enviar el PDF al usuario
        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
    }
}