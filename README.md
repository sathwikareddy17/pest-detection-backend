# Pest Detection Backend

Spring Boot 3 REST API for smart pest traps: users and login (JWT), traps, pest detections, sensor readings and alerts. It is built to serve the `pest-detection-frontend` React app without changes to the frontend's API layer.

## Run

Requires Java 17+ and Maven.

```bash
mvn spring-boot:run
```

The API starts on http://localhost:8080. The first start creates an H2 database in `./data` and fills it with demo data.

| Account | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN, can manage users and delete traps |
| `priya` | `field123` | USER |

Change these before exposing the API: set `ADMIN_PASSWORD` and `USER_PASSWORD` before the first start, or `SEED_ENABLED=false` to skip demo data entirely.

Then start the frontend (`npm run dev` in `pest-detection-frontend`). Its Vite dev server already proxies `/api` to port 8080.

```bash
mvn test        # integration tests: login, permissions, trap/detection/alert lifecycle
```

## Endpoints

All `/api/**` routes except `/api/auth/**` need `Authorization: Bearer <token>`.

| Method and path | What it does | Who |
|---|---|---|
| `POST /api/auth/login` | `{username, password}` returns `{token, username, role}` | anyone |
| `POST /api/auth/register` | `{username, email, password}` creates a USER and returns a token | anyone |
| `GET /api/users/me` | current profile | signed in |
| `GET /api/users`, `GET/PUT/DELETE /api/users/{id}` | manage accounts | ADMIN |
| `GET/POST /api/traps`, `GET/PUT /api/traps/{id}` | list, create, view, edit traps | signed in |
| `DELETE /api/traps/{id}` | delete a trap and all its history | ADMIN |
| `GET /api/detections`, `GET /api/detections/trap/{trapId}` | list detections | signed in |
| `POST /api/detections` | `{trapId, pestType, count?, confidence?, detectedAt?}` | signed in |
| `DELETE /api/detections/{id}` | delete one detection | signed in |
| `GET /api/sensors`, `GET /api/sensors/trap/{trapId}` | list readings | signed in |
| `POST /api/sensors` | `{trapId, temperature, humidity, recordedAt?}` | signed in |
| `GET /api/alerts` | list alerts | signed in |
| `PUT /api/alerts/{id}/read`, `PUT /api/alerts/{id}/resolve` | update an alert | signed in |
| `DELETE /api/alerts/{id}` | delete an alert | signed in |

Lists come back oldest first. Errors always look like `{"status": 400, "message": "..."}`.

## Alert rules

Alerts are raised automatically when data arrives. While an alert for the same problem is open, the rule does not raise another. Resolving it re-arms the rule.

| Rule | Severity |
|---|---|
| 10 or more insects of one species at one trap within 24 hours | HIGH |
| Humidity 88% or higher | MEDIUM |
| Temperature 36 °C or higher | HIGH |
| Trap battery below 20% (when saved on the trap) | LOW |

Thresholds are constants at the top of `AlertService`.

## Configuration

Set as environment variables, or edit `application.properties`.

| Variable | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | a placeholder | Signing key, 32+ characters. **Set this in production.** |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | H2 file in `./data` | Database connection |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:4173` | Browser origins allowed to call the API |
| `SEED_ENABLED` | `true` | Create demo users and farm data when the database is empty |

### Using MySQL

1. Uncomment the `mysql-connector-j` dependency in `pom.xml`.
2. Run with `DB_URL=jdbc:mysql://localhost:3306/pestdb DB_USER=... DB_PASSWORD=...`. Tables are created automatically.

## Connecting real trap hardware

A trap device can call `POST /api/detections` and `POST /api/sensors` with a token from `POST /api/auth/login`. For production devices, create a dedicated account per fleet, since tokens expire after 24 hours.
