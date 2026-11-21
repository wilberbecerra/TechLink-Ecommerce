---
name: techlink-design
description: Refactorizar las vistas Thymeleaf de TechLink con una identidad visual coherente, preservando rutas, formularios y flujos de compra. Basada en los principios de Hallmark y adaptada al catálogo real del repositorio.
---

Diseña desde lo que se vende: computación, audio, gaming y hogar/oficina. Inspecciona los controladores, modelos y fotografías de `images/` antes de decidir contenido. Lee [el contexto y las rutas](references/project.md) para ubicar vistas y capturas.

Adapta los principios de [Hallmark](https://github.com/Nutlope/hallmark) a Spring MVC. Esta skill es una adaptación independiente; no requiere Next.js, cambiar el backend ni aplicar un formato de landing a las pantallas administrativas.

Antes de editar, identifica los enlaces, acciones de formularios, nombres de campos, condiciones y bucles Thymeleaf. Conserva las rutas existentes; si un cambio funcional requiere otro método HTTP, actualiza todos sus llamadores y pruebas. Respeta modificaciones previas del usuario.

Usa el sistema documentado en `docs/design.md` y los tokens de `static/css/techlink.css`. Aplica el mismo logo, navegación, controles, moneda y tratamiento de imágenes en toda la tienda. Los precios se muestran en soles, con dos decimales. Las tablas administrativas priorizan lectura y acciones claras; no les añadas un hero publicitario.

Da protagonismo a fotografías existentes con `object-fit: contain`: no estires productos ni inventes renders. No inventes testimonios, descuentos, envío gratuito, estadísticas ni garantías. Distingue los productos y precios de demostración de datos comerciales verificados en la documentación.

Mantén encabezados legibles, cuerpo de al menos 16 px, foco visible y contraste suficiente. Evita gradientes decorativos, títulos con una palabra coloreada, etiquetas en mayúsculas espaciadas y cuadrículas de tarjetas vacías. Usa iconos de una misma familia y texto accesible para controles sin etiqueta visible.

Implementa estados reales: carrito vacío, búsqueda sin resultados, inicio de sesión fallido, falta de stock, registro inválido y envío de formulario. Los botones deben ejecutar su función; un mensaje de éxito requiere que la operación haya terminado. Conserva CSRF y autorización del servidor.

Valida vistas públicas y protegidas, tanto con administrador como con cliente. Comprueba 375, 768 y 1440 px, navegación por teclado, recursos sin 404 y ausencia de desbordamiento horizontal. Prueba compra y PDF con datos locales. Registra qué verificaste y qué quedó pendiente; no declares revisiones de navegador o Hallmark que no ejecutaste.

Actualiza `docs/capturas.md` cuando cambie la preparación de una pantalla. No reemplaces capturas antiguas por imágenes del diseño sin comprobar que la aplicación real renderiza esa vista.

Procedencia: Hallmark, commit `04977e15f3dfd6ee4b74c887dac3eb52665d6c92`, consultado el 21 de noviembre de 2026. Se adaptan principios de propósito, material real, accesibilidad y preservación del comportamiento. [Licencia original](LICENSE).
