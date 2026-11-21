# TechLink

Spring Boot 3.5.6, Java 17, Thymeleaf, Spring Security, JPA, SQL Server y JasperReports. Imágenes externas al JAR en `images/`, servidas por `ResourceWebConfiguration`. Arranca desde la raíz para resolverlas.

Vistas en `src/main/resources/templates/`: `usuario` contiene tienda y compra, `productos` contiene inventario, `administrador` contiene panel, usuarios y órdenes. Los fragmentos compartidos están en `fragments/layout.html`.

Rutas que deben conservarse: `/`, `/usuario/login`, `/usuario/registro`, `/productohome/{id}`, `/getCart`, `/order`, `/compraExitosa/{id}`, `/usuario/compras`, `/usuario/detalle/{id}`, `/administrador`, `/productos`, `/productos/create`, `/productos/edit/{id}`, `/administrador/usuarios`, `/administrador/ordenes`, `/administrador/detalle/{id}` y `/pdf/{id}`.

La carga demo solo actúa sobre una base vacía con `TECHLINK_DEMO_ENABLED=true`. Credenciales y recorrido de capturas en `docs/capturas.md`; no supongas IDs si la base ya tenía datos.

Los archivos originales de `recursos/` son evidencia histórica del README; no son las referencias visuales definitivas del rediseño.
