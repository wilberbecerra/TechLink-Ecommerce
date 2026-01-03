package com.proyecto.ecommerce.controller;

import com.proyecto.ecommerce.model.Orden;
import com.proyecto.ecommerce.model.Producto;
import com.proyecto.ecommerce.service.IOrdenService;
import com.proyecto.ecommerce.service.IUsuarioService;
import com.proyecto.ecommerce.service.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/administrador")
public class AdministradoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IOrdenService ordenService;

    private Logger logg = LoggerFactory.getLogger(AdministradoController.class);

    @GetMapping("")
    public String home(Model model) {

        List<Producto> productos = productoService.findAll();
        model.addAttribute("productos", productos);

        return "administrador/home";
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model){
        model.addAttribute("usuarios", usuarioService.findAll());
        return "administrador/usuarios";
    }

    @GetMapping("/ordenes")
    public String ordenes(Model model){
        model.addAttribute("ordenes", ordenService.findAll());
        return "administrador/ordenes";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(Model model, @PathVariable Integer id){
        logg.info("Id de la orden {} ", id);
        Orden orden = ordenService.findById(id).get();

        // --- CORRECCIÓN ---
        // Se cambió orden.getDetalle() por orden.getDetalles() (plural)
        model.addAttribute("detalles", orden.getDetalles());

        return "administrador/detalleorden";
    }

}