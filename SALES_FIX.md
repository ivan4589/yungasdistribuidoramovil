# Preventas, destinatarios WhatsApp y recibos

Cambios exclusivamente en Android; no requieren una migracion nueva ni despliegue web.
La rama incluye tambien la correccion previa de arranque (Room 5 y recuperacion
de preferencias cifradas), documentada en STARTUP_FIX.md, aun ausente de master.

- ADMIN y VENDEDOR acceden a Editar solo en preventas PENDING. El backend sigue
  validando rol, permisos y estado al guardar. Precio manual y cambios de descuento
  siguen restringidos a ADMIN.
- La edicion precarga cliente, productos, cantidades, precios, descuento,
  observaciones, modalidad, vencimiento y bonificaciones. Cuenta las unidades ya
  reservadas por esa misma preventa. Usa PATCH /api/sales/:id, sin campos de pago.
- WhatsApp consulta cliente y contactos actuales. Incluye principal y todos los
  contactos activos con telefono y consentimiento, en lotes de hasta 20 mediante
  POST /api/sales/:id/whatsapp/recipients. No reenvia automaticamente documentos
  previamente enviados. Un resultado SENT significa aceptado por Meta, no entregado.
  Los fallos individuales o resultados desconocidos se muestran sin declarar exito total.
- Ver PDF usa GET /api/documents/sales/:id, con la misma autenticacion de la app.
  El selector Android permite guardar el archivo en la ubicacion elegida; despues
  se intenta abrir con un lector PDF. Solo se habilita para ventas confirmadas.
- La cache Room actualiza cabecera, detalles y bonificaciones en una transaccion.

## Validacion manual (ADMIN y VENDEDOR)

1. Abrir una preventa pendiente, editar cantidades/productos/observaciones y guardar.
   Volver a abrir y comprobar total y detalles. El ADMIN puede modificar el precio;
   VENDEDOR conserva las restricciones del servidor.
2. En una venta confirmada, comprobar que Editar no aparece. Ver PDF debe abrir el
   selector de destino, guardar un PDF legible y abrirlo si hay lector instalado.
3. Con autorizacion del titular, probar WhatsApp en un cliente con principal y dos
   contactos autorizados; los inactivos o sin consentimiento no deben recibirlo.
   Revisar el historial web para verificar entrega. No confundir aceptacion con entrega.
4. Probar sin red: mostrar error sin crear otra venta ni repetir mensajes a ciegas.

Las pruebas automaticas de contrato usan MockWebServer; no envian mensajes reales
ni modifican ventas del servidor. No reinstalar APKs anteriores a la correccion Room 5.

## Pruebas automaticas

42 pruebas unitarias aprobadas, sin fallos ni errores: incluye 14 casos nuevos de
edicion, permisos, stock reservado, contrato PATCH, PDF y destinatarios WhatsApp.
Compilacion debug y androidTest completadas correctamente. Actualizacion instalada
en HONOR ELN-L03 con adb install -r y 7 pruebas instrumentadas aprobadas (migraciones
y recuperacion de sesion), sin desinstalar ni borrar datos. Arranque verificado.
Los envios reales de WhatsApp y la verificacion funcional con ventas reales quedan
para revision del usuario; no se enviaron mensajes ni se modificaron ventas reales.

Comando de validacion (Windows; configurar JAVA_HOME y ANDROID_HOME):

```text
gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest -Pkotlin.compiler.execution.strategy=in-process --max-workers=1 --console=plain
```

La opcion in-process evita los timeouts observados en la comunicacion con el daemon
de Kotlin en el equipo de desarrollo. No cambia la configuracion de produccion.
