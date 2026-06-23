# Pruebas de carga JMeter — IngresoUniversitarioTPI135 (backend)

## Endpoints elegidos y por qué

Los 3 planes ejercitan los mismos 3 endpoints reales:

- `GET /ingreso/resources/v1/areas`
- `GET /ingreso/resources/v1/carreras`
- `GET /ingreso/resources/v1/aulas`

Criterios de selección:

1. **Son de solo lectura (GET) y no requieren autenticación** — ninguna Resource del backend usa `@RolesAllowed`/`@DenyAll`, así que no hay credenciales que simular.
2. **No tienen efectos secundarios**: no insertan, actualizan ni borran datos, por lo que se pueden ejecutar repetidamente sin corromper el dataset de prueba (a diferencia de `POST /aspirantes`, `POST /inscripciones_prueba`, etc., que si se sometieran a carga real generarían miles de registros basura).
3. **Son catálogos consultados con alta frecuencia** durante el flujo real de inscripción (el aspirante consulta áreas de conocimiento, carreras disponibles y aulas asignadas), por lo que representan el patrón de lectura con mayor probabilidad de carga concurrente real.
4. `GET /areas` es además el mismo endpoint que ya usa `BaseSistemaBDD.esperarDespliegueApp()` como sonda de "¿el WAR ya está desplegado?", así que sirve como ancla de comparación entre el healthcheck funcional y el comportamiento bajo carga.

No se incluyen endpoints de escritura (`POST`/`PUT`/`DELETE`) porque el backend no tiene un mecanismo de reseteo de datos entre corridas, y generar datos falsos masivamente violaría las reglas de no alterar el comportamiento de negocio ni el contenido real de la base de datos.

## Planes incluidos

| Plan | Usuarios | Ramp-up | Duración | Objetivo |
|---|---|---|---|---|
| `smoke.jmx` | 1 | 1s | 1 pasada | Verificación rápida post-deploy: cada endpoint debe responder 200. |
| `load.jmx` | 20 concurrentes | 10s | 120s | Carga normal esperada (think time 0.3–1s entre requests). |
| `stress.jmx` | 150 concurrentes | 30s | 180s | Sin think time. Busca el punto de quiebre (pool JDBC, threads de Liberty). |

Los 3 planes parametrizan `host` y `port` (por defecto `localhost:9080`), sobreescribibles por línea de comandos.

## Ejecución

Requiere [Apache JMeter](https://jmeter.apache.org/) instalado (no se agrega como dependencia Maven para no introducir un plugin nuevo en el build).

```bash
# Contra un backend ya desplegado en localhost:9080 (docker compose up -d)
jmeter -n -t jmeter/smoke.jmx -l jmeter/results/smoke-result.jtl -e -o jmeter/results/smoke-report

jmeter -n -t jmeter/load.jmx -l jmeter/results/load-result.jtl -e -o jmeter/results/load-report

jmeter -n -t jmeter/stress.jmx -l jmeter/results/stress-result.jtl -e -o jmeter/results/stress-report

# Contra otro host/puerto (ej. un ambiente de staging)
jmeter -n -t jmeter/load.jmx -Jhost=staging.example.com -Jport=9080 -l jmeter/results/load-result.jtl
```

`-n` modo no interactivo (CLI), `-l` archivo de resultados `.jtl`, `-e -o` genera un reporte HTML navegable en la carpeta indicada.

Los resultados (`jmeter/results/`) no deben versionarse — agregar `jmeter/results/` al `.gitignore` si se generan localmente.
