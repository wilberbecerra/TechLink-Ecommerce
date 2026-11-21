# Accesos y recorrido para las capturas

Inicia TechLink desde la raíz con `powershell -ExecutionPolicy Bypass -File scripts/start-local.ps1` y abre [la tienda](http://localhost:8080/). Para otra instalación, prepara primero la base con `scripts/setup-database.ps1`.

## Cuentas de demostración

Estas cuentas se crean únicamente al arrancar con `TECHLINK_DEMO_ENABLED=true` sobre una base completamente vacía. No se recuperaron contraseñas anteriores ni se restablecen cuentas existentes.

Los accesos de administrador y cliente se consultan únicamente en `.local/demo-credentials.json`, generado o conservado por el script de arranque y excluido de Git. La contraseña SQL está en `.local/db-password.txt`; es independiente de las cuentas de la web. No publiques estos archivos ni incluyas contraseñas en las capturas.

Ambas ingresan desde [Iniciar sesión](http://localhost:8080/usuario/login). Para cambiar de cuenta, abre el nombre de usuario en el encabezado y pulsa **Cerrar sesión**. También puedes usar dos perfiles de navegador.

## Capturas del README original

Las rutas se conservaron. Los IDs de ejemplo corresponden a la carga inicial de una base vacía; si tu base ya tiene información, entra mediante los botones de la aplicación para obtener los IDs correctos.

| Captura / archivo en `recursos/` | Enlace | Preparación |
| --- | --- | --- |
| Página principal / `pagina-principal.jpg` | [Vista general](http://localhost:8080/administrador) | Ingresar como administrador. Esta es la ruta visible en la captura original. |
| Login / `login.jpg` | [Ingresar](http://localhost:8080/usuario/login) | Cerrar sesión. No mostrar contraseñas en la captura. |
| Lista de productos / `lista-productos.jpg` | [Inventario](http://localhost:8080/productos) | Administrador. |
| Crear producto / `create-productos.jpg` | [Nuevo producto](http://localhost:8080/productos/create) | Administrador. No hace falta guardar. |
| Editar producto / `edit-productos.jpg` | [Editar producto 1](http://localhost:8080/productos/edit/1) | Administrador. No hace falta modificarlo. |
| Lista de usuarios / `lista-usuarios.jpg` | [Usuarios](http://localhost:8080/administrador/usuarios) | Administrador. |
| Lista de órdenes / `lista-ordenes.jpg` | [Órdenes](http://localhost:8080/administrador/ordenes) | Administrador. La demo incluye una compra. |
| Detalle de orden / `detalles.jpg` | [Orden 1](http://localhost:8080/administrador/detalle/1) | Administrador. La captura antigua mostraba el ID 2, que depende de cada base. |
| Reporte de orden / `orden-pdf.jpg` | [Comprobante 1](http://localhost:8080/pdf/1) | Administrador o cliente dueño de la orden. Abre el PDF en el navegador. |

`reporte-orden.jpg` es otro archivo histórico presente en el repositorio. Si lo usas, reemplázalo con la captura del mismo comprobante.

## Capturas adicionales de la tienda renovada

| Pantalla | Enlace | Preparación |
| --- | --- | --- |
| Tienda pública | [Inicio](http://localhost:8080/) | Sin sesión o como cliente. |
| Categoría gaming | [Gaming](http://localhost:8080/?categoria=Gaming#catalogo) | Filtro real del catálogo. |
| Detalle del producto | [Sony WH-1000XM5](http://localhost:8080/productohome/9) | ID 9 en la carga inicial. |
| Registro | [Crear cuenta](http://localhost:8080/usuario/registro) | Sin sesión. |
| Carrito | [Tu carrito](http://localhost:8080/getCart) | Entra al detalle de un producto y pulsa **Agregar al carrito**. |
| Resumen | [Confirmar compra](http://localhost:8080/order) | Cliente con productos en el carrito. |
| Compra confirmada | [Orden de demostración](http://localhost:8080/compraExitosa/1) | Cliente demo o administrador. Para una compra nueva, utiliza la redirección posterior a confirmar. |
| Mis compras | [Compras del cliente](http://localhost:8080/usuario/compras) | Cliente demo. |
| Detalle de compra | [Compra 1](http://localhost:8080/usuario/detalle/1) | Cliente demo, propietario de la orden. |

Para un recorrido completo: cliente → catálogo → detalle → agregar al carrito → continuar compra → confirmar → comprobante PDF. Confirmar guarda una orden y descuenta stock; usa la orden demo para fotografiar sin generar compras adicionales.

Se recomienda 1440 × 1000 para escritorio y 390 × 844 para móvil, zoom 100 %. Para la portada, utiliza captura de página completa si quieres incluir el catálogo. Mantén el tamaño de ventana entre capturas y cierra el menú de cuenta antes de fotografiar. Las imágenes históricas se conservan hasta que las reemplaces. El README usa rutas relativas para que aparezcan las capturas nuevas al actualizar los archivos.
