# liga-futbol-api — Relaciones Spring + MongoDB

API REST con Spring Boot 3 + Spring Data MongoDB que modela las 4 relaciones del taller
usando `@Document(collection = "club")` y `@DocumentReference`.

## Cómo quedan las relaciones

| JPA | Mongo (en `Club`) | Cómo se guarda en la colección `club` |
|---|---|---|
| `@OneToOne` | `@DocumentReference Entrenador entrenador` | `"entrenador": ObjectId(...)` |
| `@OneToMany` | `@DocumentReference List<Jugador> jugadores` | `"jugadores": [ObjectId, ...]` |
| `@ManyToOne` | `@DocumentReference Asociacion asociacion` | `"asociacion": ObjectId(...)` |
| `@ManyToMany` | `@DocumentReference List<Competicion> competiciones` | `"competiciones": [ObjectId, ...]` |

El `Club` es el dueño de todas las relaciones (unidireccionales, igual que en el PPT).
`@DocumentReference` guarda solo el `_id` (como una FK) y Spring lo resuelve al consultar.

Como Mongo no tiene FK, las reglas se hacen en `ClubService`:
- Un entrenador no puede estar en 2 clubes (1:1) → 409.
- Un jugador no puede estar en 2 clubes (1:N) → 409.
- Borrar un club borra su entrenador y sus jugadores (como `@OnDelete CASCADE`).
- Borrar una asociación con clubes afiliados → 409 (como `ON DELETE NO ACTION`).
- Borrar una competición / jugador / entrenador lo saca de los clubes.

**Eager / Lazy:** por defecto `@DocumentReference` carga eager (resuelve al leer el club).
Para lazy se usa `@DocumentReference(lazy = true)` y solo consulta cuando se accede al atributo.

## Ejecutar

Requisitos: Java 17+ y Maven. En IntelliJ: File → Open → carpeta `liga-futbol-api` → Run en `LigaFutbolApplication`.

Interfaz web: http://localhost:8080/  ·  API: http://localhost:8080/api/clubes

```bash
mvn spring-boot:run
```

En MongoDB Atlas → **Network Access**, agrega tu IP (o `0.0.0.0/0` para pruebas), si no la conexión falla.

La URI está en `src/main/resources/application.properties` (base de datos `liga`).
Al primer arranque carga datos de ejemplo (Atletico Nacional, Junior, DIMAYOR, 3 competiciones, más 1 entrenador y 2 jugadores libres).
Se desactiva con `app.seed.enabled=false`.

## Endpoints

Base: `http://localhost:8080/api`

| Recurso | Endpoints |
|---|---|
| Entrenadores | `GET/POST /entrenadores`, `GET/PUT/DELETE /entrenadores/{id}` |
| Jugadores | `GET/POST /jugadores`, `GET/PUT/DELETE /jugadores/{id}` |
| Asociaciones | `GET/POST /asociaciones`, `GET/PUT/DELETE /asociaciones/{id}`, `GET /asociaciones/{id}/clubes` |
| Competiciones | `GET/POST /competiciones`, `GET/PUT/DELETE /competiciones/{id}`, `GET /competiciones/{id}/clubes` |
| Clubes | `GET/POST /clubes`, `GET/PUT/DELETE /clubes/{id}` |
| 1:1 | `PUT /clubes/{id}/entrenador/{entrenadorId}`, `DELETE /clubes/{id}/entrenador` |
| 1:N | `POST/DELETE /clubes/{id}/jugadores/{jugadorId}` |
| N:1 | `PUT /clubes/{id}/asociacion/{asociacionId}` |
| N:M | `POST/DELETE /clubes/{id}/competiciones/{competicionId}` |

Body para crear/actualizar club:

```json
{
  "nombre": "Atletico Nacional",
  "entrenadorId": "...",
  "jugadoresIds": ["...", "..."],
  "asociacionId": "...",
  "competicionesIds": ["...", "..."]
}
```

En `requests.http` están todas las peticiones listas (IntelliJ / extensión REST Client de VS Code)
y `postman_collection.json` para importar en Postman.


## Interfaz web renovada

La interfaz de `src/main/resources/static/index.html` fue rediseñada completamente para la exposición. Ahora utiliza un panel oscuro tipo dashboard, navegación lateral y vistas de Resumen, Clubes, Entidades, Relaciones y API REST. Todas las operaciones visuales se concentran en este único `index.html` y consumen los endpoints REST existentes mediante `fetch`, sin cambiar el modelo ni los controladores del backend.
