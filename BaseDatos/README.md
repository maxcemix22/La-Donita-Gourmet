# BaseDatos

Scripts de MySQL 8.0.13 o superior. Ejecutar **en este orden**:

| Script | Qué hace | ¿Obligatorio? |
|---|---|---|
| `01_base_datos_ladonita.sql` | Crea la base `ladonita_gourmet`, las 9 tablas y los datos iniciales | Sí |
| `02_actualizacion_sistema.sql` | Agrega `foto_url` e índices y deja las contraseñas iniciales en PBKDF2. Se puede ejecutar más de una vez | Sí |
| `03_reportes.sql` | Crea las vistas de los reportes R01–R08 para consultarlas desde Workbench | Opcional |
| `04_datos_ejemplo_OPCIONAL.sql` | Clientes y pedidos de ejemplo | **Solo pruebas o capacitación**, nunca en la base real del negocio |

```bash
mysql -u root -p < 01_base_datos_ladonita.sql
mysql -u root -p < 02_actualizacion_sistema.sql
mysql -u root -p < 03_reportes.sql
```

Nota: el **modo demo** de la aplicación (sin MySQL) no usa estos scripts. Usa una base H2 que se carga desde
`JavaNetbeans/src/main/resources/demo/ladonita-demo.sql`; ese archivo debe quedarse ahí porque se lee desde el classpath.
