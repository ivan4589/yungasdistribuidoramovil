# Correccion del cierre al arrancar

El Logcat incluido en el repositorio registra `A migration from 4 to 3 was required but not found`.
El commit 28888b0 restauro la version 4, pero MIGRATION_3_4 agregaba la columna nullable
sales.cancelledPdfUrl sin declararla en SaleEntity. Esto dejaba diferentes esquemas
version 4 segun si la instalacion era nueva o actualizada.

La correccion usa Room version 5, declara la columna y migra ambas variantes de version 4
sin borrar datos. Tambien conserva la URL en el mapper remoto/local.
La tabla bonus_rules tambien tenia dos esquemas incompatibles. La migracion
normaliza minimumQuantity a minQuantity y conserva cantidades, fechas y timestamps
existentes al reconstruir esa tabla dentro de la transaccion de Room.
No se anade fallbackToDestructiveMigration ni una ruta de downgrade.

La prueba en el HONOR tambien detecto AEADBadTagException al abrir las cookies cifradas.
Ahora se conserva el archivo ilegible y se abre un almacen cifrado de recuperacion
independiente, sin eliminar claves existentes, preferencias ni la base de datos.
Si el Keystore sigue sin estar disponible, las cookies solo viven en memoria y no
se permite persistir una sesion offline. Puede ser necesario iniciar sesion nuevamente.
Las preferencias de autenticacion se excluyen del backup y de transferencias entre
dispositivos, porque sus claves de Android Keystore no se transfieren con ellas.

## Validacion

Validado en HONOR ELN-L03 (Android 13): compilacion debug y androidTest correctas,
28 pruebas unitarias y 7 pruebas instrumentadas aprobadas. Actualizacion mediante
adb install -r, arranque correcto y proceso activo sin errores AndroidRuntime.
No se desinstalo la app, no se borro su almacenamiento ni se extrajo la base privada.
Esta validacion no sustituye pruebas funcionales de login, ventas y sincronizacion.

- `gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
- Prueba instrumentada: `com.example.yungasdistribuidora.data.local.database.AppDatabaseMigrationTest`.
  Comprueba instalaciones nuevas y migraciones 1/2/3/4 a 5, ambas variantes de version 4,
  preservacion de clientes, importes, detalles y URL de PDF, y reapertura de la base.
- Las bases de prueba tienen nombres propios; nunca se elimina yungas_distribuidora.db.
- Para un telefono con datos reales, actualizar con `adb install -r` y ejecutar la
  instrumentacion manualmente. No desinstalar la app ni borrar su almacenamiento.
- No volver a instalar APKs anteriores que declaren una version Room menor.
- Abrir esta copia del proyecto y la rama fix/room-startup-migration en Android Studio
  antes de compilar nuevamente; otra copia con master antiguo no contiene el arreglo.

Los archivos .logcat pueden contener informacion privada: no agregar nuevos volcados completos a Git.
