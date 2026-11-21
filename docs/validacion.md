# Validación local del rediseño

Comprobaciones realizadas el 21 de noviembre de 2026 en Windows, con JDK 17, Chrome y SQL Server 2022 local.

## Compilación y pruebas

`mvn -B package`: compilación y empaquetado correctos. Se ejecutaron **9 pruebas, sin fallos ni errores**, usando H2 temporal en modo SQL Server. Cubren renderizado de vistas públicas y administrativas, autenticación con las dos contraseñas demo, restricciones por rol, carrito separado por sesión, cantidades inválidas, creación de orden y descuento de stock, PDF válido, privacidad de órdenes, registro sin elevación de rol, CSRF y creación/edición/eliminación de productos.

La aplicación se inició también contra SQL Server, sin H2. Se verificaron **12 productos, 2 usuarios y 1 orden** en la base `TechLink`. El segundo arranque conservó esos datos sin duplicar la carga inicial.

Se comprobó el arranque mediante `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/start-local.ps1`. Ambos scripts PowerShell pasaron la comprobación de sintaxis. La skill pasó `quick_validate.py` y JavaScript pasó `node --check`.

## Navegador

Se completaron **45 comprobaciones con Chrome**, sin incidencias en el reporte final. Las páginas públicas y administrativas se inspeccionaron a 375, 768 y 1440 px de ancho. No se detectaron desbordamientos horizontales, errores de JavaScript ni imágenes rotas. Las imágenes diferidas se cargaron explícitamente para las capturas de página completa.

Se verificaron búsqueda sin resultados, filtro por categoría, ordenamiento de precios, adición al carrito, conservación del carrito al iniciar sesión, resumen de compra, historial, detalle y confirmación de la orden demo. Ambos usuarios de demostración iniciaron sesión; sus credenciales se conservan solo en el entorno local. Se comprobó filtro de inventario. El comprobante se descargó desde SQL Server y su respuesta fue `application/pdf` con firma `%PDF`.

El navegador no confirmó compras adicionales: la operación de compra y su efecto sobre el stock se probaron en la base temporal de las pruebas automatizadas. Las comprobaciones de navegador son funcionales y visuales; no equivalen a una auditoría completa de accesibilidad ni a pruebas de todos los navegadores.

Las capturas conservadas en `recursos/redesign/` corresponden a la aplicación ejecutada, no a maquetas. Hay una portada de escritorio, portada móvil, acceso y administración. El reporte completo y las demás capturas de revisión están en `target/visual-review/`, que Git ignora.

## Alcance de Hallmark

La skill creada es una adaptación de Hallmark para este proyecto. Se consultaron su skill original, pautas de diseño, pantallas de aplicación y controles. Se ejecutó su comprobador mecánico tanto sobre las plantillas como sobre HTML renderizado. El análisis directo de Thymeleaf no resuelve los fragmentos que aportan título, estilos y foco: por ello no se considera una certificación de las plantillas.

El último análisis del HTML renderizado reportó 14 hallazgos: 12 etiquetas de categoría, la ausencia de recorte horizontal global y una columna móvil `1fr`. Las categorías encima del nombre del producto son información de navegación del e-commerce; se conservan. No se fuerza recorte horizontal global, para que un desbordamiento real pueda detectarse en las pruebas. La columna móvil no produjo desbordamiento en los tamaños comprobados. Son diferencias respecto al comprobador original; no se afirma aprobación integral del lint ni revisión por subagentes.
