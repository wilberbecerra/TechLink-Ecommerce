package com.proyecto.ecommerce;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.proyecto.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StoreFlowTests {

  @Value("${techlink.demo.admin-password}")
  String adminPassword;

  @Value("${techlink.demo.customer-password}")
  String customerPassword;

  @Autowired
  MockMvc mvc;

  @Autowired
  IProductoRepository productos;

  @Autowired
  IOrdenRepository ordenes;

  @Autowired
  IUsuarioRepository usuarios;

  private int productId() {
    return productos.findAll().get(0).getId();
  }

  @Test
  void publicViewsRenderAndSearchFiltersRealProducts() throws Exception {
    for (String path : new String[] {
      "/",
      "/usuario/login",
      "/usuario/registro",
      "/getCart",
      "/productohome/" + productId(),
    })
      mvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(
          content().string(
            org.hamcrest.Matchers.containsString("/css/techlink.css")
          )
        );
    mvc
      .perform(get("/").param("categoria", "Audio"))
      .andExpect(status().isOk())
      .andExpect(
        model().attribute("productos", org.hamcrest.Matchers.hasSize(3))
      );
    mvc
      .perform(get("/").param("nombre", "ningun-producto-12345"))
      .andExpect(status().isOk())
      .andExpect(
        content().string(
          org.hamcrest.Matchers.containsString("No encontramos productos")
        )
      );
  }

  @Test
  void demoPasswordsAuthenticateAndFailedLoginShowsError() throws Exception {
    mvc
      .perform(
        post("/usuario/login")
          .with(csrf())
          .param("username", "admin@techlink.pe")
          .param("password", adminPassword)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/usuario/acceder"));
    mvc
      .perform(
        post("/usuario/login")
          .with(csrf())
          .param("username", "cliente@techlink.pe")
          .param("password", customerPassword)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/usuario/acceder"));
    mvc
      .perform(
        post("/usuario/login")
          .with(csrf())
          .param("username", "cliente@techlink.pe")
          .param("password", "wrong")
      )
      .andExpect(redirectedUrl("/usuario/login?error"));
  }

  @Test
  void adminViewsRenderAndCustomerCannotOpenThem() throws Exception {
    for (String path : new String[] {
      "/administrador",
      "/productos",
      "/productos/create",
      "/productos/edit/" + productId(),
      "/administrador/usuarios",
      "/administrador/ordenes",
      "/administrador/detalle/1",
    }) {
      mvc
        .perform(get(path).with(user("admin@techlink.pe").roles("ADMIN")))
        .andExpect(status().isOk());
      mvc
        .perform(get(path).with(user("cliente@techlink.pe").roles("USER")))
        .andExpect(status().isForbidden());
    }
  }

  @Test
  void cartsAreIsolatedAndInvalidQuantitiesCannotBeAdded() throws Exception {
    var first = new MockHttpSession();
    var second = new MockHttpSession();
    int id = productId();
    mvc
      .perform(
        post("/cart")
          .session(first)
          .with(csrf())
          .param("id", "" + id)
          .param("cantidad", "2")
      )
      .andExpect(redirectedUrl("/getCart"));
    mvc
      .perform(get("/getCart").session(first))
      .andExpect(model().attribute("cart", org.hamcrest.Matchers.hasSize(1)));
    mvc
      .perform(get("/getCart").session(second))
      .andExpect(model().attribute("cart", org.hamcrest.Matchers.hasSize(0)));
    mvc
      .perform(
        post("/cart")
          .session(first)
          .with(csrf())
          .param("id", "" + id)
          .param("cantidad", "-1")
      )
      .andExpect(status().isBadRequest());
    mvc
      .perform(
        post("/cart")
          .session(first)
          .with(csrf())
          .param("id", "" + id)
          .param("cantidad", "999")
      )
      .andExpect(status().isBadRequest());
    mvc
      .perform(
        post("/delete/cart/" + id)
          .session(first)
          .with(csrf())
      )
      .andExpect(redirectedUrl("/getCart"));
    mvc
      .perform(get("/getCart").session(first))
      .andExpect(model().attribute("cart", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void checkoutPersistsOrderReducesStockAndProducesPdf() throws Exception {
    var session = new MockHttpSession();
    int id = productId();
    int before = productos.findById(id).orElseThrow().getCantidad();
    long count = ordenes.count();
    mvc
      .perform(
        post("/cart")
          .session(session)
          .with(csrf())
          .param("id", "" + id)
          .param("cantidad", "1")
      )
      .andExpect(status().is3xxRedirection());
    mvc
      .perform(
        get("/order")
          .session(session)
          .with(user("cliente@techlink.pe").roles("USER"))
      )
      .andExpect(status().isOk());
    var saved = mvc
      .perform(
        post("/saveOrder")
          .session(session)
          .with(user("cliente@techlink.pe").roles("USER"))
          .with(csrf())
      )
      .andExpect(status().is3xxRedirection())
      .andReturn();
    assertThat(ordenes.count()).isEqualTo(count + 1);
    assertThat(productos.findById(id).orElseThrow().getCantidad()).isEqualTo(
      before - 1
    );
    mvc
      .perform(
        get(saved.getResponse().getRedirectedUrl()).with(
          user("cliente@techlink.pe").roles("USER")
        )
      )
      .andExpect(status().isOk());
    mvc
      .perform(get("/pdf/1").with(user("cliente@techlink.pe").roles("USER")))
      .andExpect(status().isOk())
      .andExpect(content().contentType("application/pdf"))
      .andExpect(result ->
        assertThat(result.getResponse().getContentAsByteArray()).startsWith(
          "%PDF".getBytes()
        )
      );
    mvc
      .perform(
        get("/usuario/compras").with(user("cliente@techlink.pe").roles("USER"))
      )
      .andExpect(status().isOk());
    mvc
      .perform(
        get("/usuario/detalle/1").with(
          user("cliente@techlink.pe").roles("USER")
        )
      )
      .andExpect(status().isOk());
    mvc
      .perform(get("/getCart").session(session))
      .andExpect(model().attribute("cart", org.hamcrest.Matchers.hasSize(0)));
  }

  @Test
  void ordersArePrivateAndCsrfProtectsMutations() throws Exception {
    var other = new com.proyecto.ecommerce.model.Usuario(
      null,
      "Otra persona",
      "other",
      "other@techlink.pe",
      "Lima",
      null,
      "USER",
      "unused"
    );
    usuarios.save(other);
    for (String path : new String[] {
      "/pdf/1",
      "/compraExitosa/1",
      "/usuario/detalle/1",
    })
      mvc
        .perform(get(path).with(user("other@techlink.pe").roles("USER")))
        .andExpect(status().isForbidden());
    mvc
      .perform(
        post("/cart")
          .param("id", "" + productId())
          .param("cantidad", "1")
      )
      .andExpect(status().isForbidden());
  }

  @Test
  void registrationIgnoresSubmittedRoleAndRejectsDuplicateEmails() throws Exception {
    mvc
      .perform(
        post("/usuario/save")
          .with(csrf())
          .param("nombre", "Nuevo Cliente")
          .param("email", "nuevo@techlink.pe")
          .param("direccion", "Lima")
          .param("password", "ClienteSeguro!2026")
          .param("tipo", "ADMIN")
          .param("id", "1")
      )
      .andExpect(redirectedUrl("/usuario/login?registered"));
    assertThat(
      usuarios.findByEmail("nuevo@techlink.pe").orElseThrow().getTipo()
    ).isEqualTo("USER");
    mvc
      .perform(
        post("/usuario/save")
          .with(csrf())
          .param("nombre", "Otro")
          .param("email", "cliente@techlink.pe")
          .param("direccion", "Lima")
          .param("password", "Seguro!2026")
      )
      .andExpect(status().isOk())
      .andExpect(model().attributeExists("errorRegistro"));
  }

  @Test
  void adminCanManageProductAndCannotDeleteHistoricalProduct() throws Exception {
    mvc
      .perform(
        multipart("/productos/save")
          .file(
            new org.springframework.mock.web.MockMultipartFile(
              "img",
              new byte[0]
            )
          )
          .with(user("admin@techlink.pe").roles("ADMIN"))
          .with(csrf())
          .param("nombre", "Producto de prueba")
          .param("descripcion", "Descripción")
          .param("cantidad", "2")
          .param("precio", "25.50")
      )
      .andExpect(redirectedUrl("/productos"));
    var p = productos
      .findAll()
      .stream()
      .filter(x -> x.getNombre().equals("Producto de prueba"))
      .findFirst()
      .orElseThrow();
    mvc
      .perform(
        multipart("/productos/update")
          .file(
            new org.springframework.mock.web.MockMultipartFile(
              "img",
              new byte[0]
            )
          )
          .with(user("admin@techlink.pe").roles("ADMIN"))
          .with(csrf())
          .param("id", "" + p.getId())
          .param("nombre", "Producto editado")
          .param("descripcion", "Descripción nueva")
          .param("cantidad", "3")
          .param("precio", "29.50")
      )
      .andExpect(redirectedUrl("/productos"));
    assertThat(
      productos.findById(p.getId()).orElseThrow().getNombre()
    ).isEqualTo("Producto editado");
    mvc
      .perform(
        post("/productos/delete/" + p.getId())
          .with(user("admin@techlink.pe").roles("ADMIN"))
          .with(csrf())
      )
      .andExpect(redirectedUrl("/productos"));
    assertThat(productos.findById(p.getId())).isEmpty();
    int historical = ordenes
      .findById(1)
      .orElseThrow()
      .getDetalles()
      .get(0)
      .getProducto()
      .getId();
    mvc
      .perform(
        post("/productos/delete/" + historical)
          .with(user("admin@techlink.pe").roles("ADMIN"))
          .with(csrf())
      )
      .andExpect(redirectedUrl("/productos?inUse"));
  }
}
