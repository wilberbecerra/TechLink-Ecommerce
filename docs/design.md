# Identidad visual de TechLink

Propuesta inicial del 21 de noviembre de 2026, abierta a los archivos de referencia de Eduardo. Aplicada a las vistas reales de Spring Boot y Thymeleaf.

La idea es conectar las piezas del espacio personal: computadora, consola, audio y accesorios. El símbolo del logo une dos eslabones; el nombre aparece en minúsculas con un punto cobalto. Los archivos editables están en `src/main/resources/static/brand/techlink.svg` y `mark.svg`.

La variante `src/main/resources/static/brand/techlink-white.svg` utiliza letras blancas y conserva el símbolo y el punto cobalto. El README selecciona esta variante en modo oscuro mediante `<picture>` y mantiene las letras oscuras para el modo claro.

El catálogo se apoya en superficies claras, texto oscuro, azul cobalto y fotografías existentes. El precio y la disponibilidad acompañan a cada producto. Se evita inventar descuentos, estrellas, testimonios, garantía o condiciones de envío.

| Rol | Valor |
| --- | --- |
| Fondo | `#f6f7fb` |
| Superficie | `#ffffff` |
| Texto | `#151c31` |
| Texto secundario | `#606879` |
| Acción principal | `#284bfa` |
| Acción al pasar el cursor | `#1b36c1` |
| Superficie de énfasis | `#edf0ff` |
| Borde | `#e1e5ef` |
| Tipografía | Segoe UI, Arial y sans-serif del sistema |

Los tokens están en `techlink.css`. Contenedor máximo de 1280 px; tarjetas de producto con imagen completa, categoría, nombre, precio y disponibilidad. Tablas con cifras alineadas y acciones con etiquetas accesibles. Las pantallas administrativas usan una navegación lateral que se convierte en barra superior en pantallas pequeñas.

Se usan enlaces para navegar y formularios POST con CSRF para las mutaciones. Las imágenes se sirven localmente; no hay fuentes, Bootstrap, jQuery ni fotografías remotas necesarios para renderizar la tienda. El ordenamiento, vista de contraseña, filtro de inventario y previsualización de imágenes usan JavaScript pequeño y progresivo.

La skill `skills/techlink-design` adapta principios de [Hallmark](https://github.com/Nutlope/hallmark), commit `04977e15f3dfd6ee4b74c887dac3eb52665d6c92`. Su licencia MIT se conserva dentro de la skill. La implementación no utiliza el sistema completo de temas ni exige sus reglas de landing a las pantallas de administración.

Los nombres, precios, stock y descripciones de la carga inicial son datos de demostración reconstruidos desde las imágenes, capturas y el código del proyecto. No representan un catálogo comercial verificado. El flujo confirma una orden local y genera un PDF; no integra una pasarela de pagos. El comprobante es académico y conserva los datos del reporte original; no es un documento tributario válido.
