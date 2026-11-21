package com.proyecto.ecommerce.config;

import com.proyecto.ecommerce.model.*;
import com.proyecto.ecommerce.repository.*;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "techlink.demo.enabled", havingValue = "true")
public class DemoData implements ApplicationRunner {

  private final IUsuarioRepository usuarios;
  private final IProductoRepository productos;
  private final IOrdenRepository ordenes;
  private final IDetalleOrdenRepository detalles;
  private final BCryptPasswordEncoder encoder;
  private final String adminPassword;
  private final String customerPassword;

  public DemoData(
    IUsuarioRepository u,
    IProductoRepository p,
    IOrdenRepository o,
    IDetalleOrdenRepository d,
    BCryptPasswordEncoder e,
    @Value("${techlink.demo.admin-password:}") String adminPassword,
    @Value("${techlink.demo.customer-password:}") String customerPassword
  ) {
    usuarios = u;
    productos = p;
    ordenes = o;
    detalles = d;
    encoder = e;
    this.adminPassword = adminPassword;
    this.customerPassword = customerPassword;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (
      usuarios.count() != 0 || productos.count() != 0 || ordenes.count() != 0
    ) return;
    if (adminPassword.length() < 12 || customerPassword.length() < 12) {
      throw new IllegalStateException(
        "Configura contraseñas demo de al menos 12 caracteres mediante las variables de entorno o scripts/start-local.ps1."
      );
    }
    Usuario admin = usuarios.save(
      new Usuario(
        null,
        "Administrador",
        "admin",
        "admin@techlink.pe",
        "San Miguel, Lima",
        "999111222",
        "ADMIN",
        encoder.encode(adminPassword)
      )
    );
    Usuario cliente = usuarios.save(
      new Usuario(
        null,
        "Juan Pérez",
        "juan",
        "cliente@techlink.pe",
        "Av. Siempre Viva 123, Lima",
        "999333444",
        "USER",
        encoder.encode(customerPassword)
      )
    );
    Object[][] catalogo = {
      {
        "Monitor Lenovo ThinkVision 23.8 pulgadas IPS",
        "Monitor Full HD de 23.8 pulgadas con panel IPS y conexiones HDMI y VGA. Más espacio para tus tareas y tu setup.",
        "1_1000_2.jpg",
        520.0,
        20,
      },
      {
        "Samsung Smart TV 55 pulgadas 4K UHD",
        "Pantalla Crystal UHD de 55 pulgadas, resolución 4K, HDR y Wi-Fi. Tus películas en una pantalla más grande.",
        "TV.jpg",
        1899.0,
        10,
      },
      {
        "Blue Yeti Micrófono USB Profesional",
        "Micrófono USB con cuatro patrones de captación. Para tus streams, podcasts y videollamadas.",
        "yeti.jpg",
        480.0,
        15,
      },
      {
        "Epson EcoTank L3250",
        "Multifuncional 3 en 1 con tanque de tinta y Wi-Fi. Imprime, copia y escanea desde tu espacio de trabajo.",
        "impresora.jpg",
        750.0,
        8,
      },
      {
        "Tablet Huawei MediaPad",
        "Una tablet para tus lecturas, entretenimiento y tareas cotidianas. Pantalla táctil y diseño compacto.",
        "producto1.jpg",
        599.0,
        12,
      },
      {
        "ASUS TUF Gaming F15",
        "Intel Core i5 de 11.ª generación, NVIDIA GeForce RTX 3050, 16 GB de RAM y pantalla de 144 Hz.",
        "ASUS TUF Gaming F15.jpg",
        3200.0,
        4,
      },
      {
        "Nintendo Switch OLED - Modelo Blanco",
        "Consola híbrida con pantalla OLED de 7 pulgadas y controles Joy-Con blancos. Juega en casa o llévala contigo.",
        "Nintendo Switch OLED - Modelo Blanco.jpg",
        1450.0,
        9,
      },
      {
        "Teclado Mecánico Logitech G Pro X",
        "Teclado mecánico compacto para jugar. Formato sin teclado numérico para dar más espacio a tu mouse.",
        "Teclado Mecánico Logitech G Pro X.jpg",
        549.0,
        14,
      },
      {
        "Sony WH-1000XM5 Noise Cancelling",
        "Audífonos inalámbricos con cancelación de ruido. Hasta 30 horas de batería para escuchar tu música y concentrarte.",
        "Sony WH-1000XM5 Noise Cancelling.jpg",
        1299.0,
        7,
      },
      {
        "Mouse Razer DeathAdder V2",
        "Mouse gamer con diseño ergonómico y conexión USB. Precisión y comodidad para tus partidas.",
        "Mouse Razer DeathAdder V2.jpg",
        229.0,
        20,
      },
      {
        "Silla Gamer Corsair T3 Rush",
        "Silla con respaldo reclinable y soporte lumbar. Un espacio cómodo para jugar y trabajar.",
        "Silla Gamer Corsair T3 Rush.jpg",
        1100.0,
        6,
      },
      {
        "Amazon Echo Dot (5ta Gen) con Reloj",
        "Parlante inteligente con Alexa y reloj integrado. Música, temporizadores y ayuda para tu día a día.",
        "Amazon Echo Dot (5ta Gen) con Reloj.jpg",
        249.0,
        16,
      },
    };
    for (Object[] p : catalogo)
      productos.save(
        new Producto(
          null,
          (String) p[0],
          (String) p[1],
          (String) p[2],
          (Double) p[3],
          (Integer) p[4],
          admin
        )
      );
    var all = productos.findAll();
    Orden orden = new Orden(
      null,
      "0000000001",
      Date.from(Instant.parse("2026-11-20T20:00:00Z")),
      null,
      5450
    );
    orden.setUsuario(cliente);
    ordenes.save(orden);
    addDetalle(orden, all.get(6), 3);
    addDetalle(orden, all.get(10), 1);
  }

  private void addDetalle(Orden o, Producto p, int cantidad) {
    var d = new DetalleOrden(
      null,
      p.getNombre(),
      cantidad,
      p.getPrecio(),
      p.getPrecio() * cantidad
    );
    d.setOrden(o);
    d.setProducto(p);
    detalles.save(d);
  }
}
