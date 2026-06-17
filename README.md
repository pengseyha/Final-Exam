# ID Card Manager

A Spring Boot application for managing ID cards for **students, employees and users**.

## Features

| Feature | Where |
| --- | --- |
| CRUD for profiles | `/profiles` (web UI) and `/api/profiles` (JSON REST) |
| Photo upload (JPEG/PNG, validated type & size) | create/edit form → stored on disk |
| ID-card template engine (Thymeleaf + CSS) | `/templates` |
| Live preview (instant, as you type) | `/profiles/new` preview pane |
| Unique ID / registration number (`YEAR-DEPT-###`) + UUID | auto-assigned on create |
| PDF export (iText 7) | `/profiles/{id}/pdf` |
| Batch ID-card generation (ZIP of PDFs) | `/batch/type/STUDENT`, `/batch` |
| QR code (ZXing) | `/profiles/{id}/qr` — encodes the verification URL |
| Barcode (Code-128 / EAN-13, ZXing) | `/profiles/{id}/barcode` |
| Public verification page | `/verify/{uuid}` |

## Tech stack

- Java 25, Spring Boot 4 (Spring MVC, Spring Data JPA)
- MySQL 8 (production) / H2 (tests and the `h2` dev profile)
- Thymeleaf, iText 7, ZXing, Lombok

---

## Run locally

### With MySQL (default profile)
```bash
./mvnw spring-boot:run
```
Override DB credentials with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` environment variables.

### Without MySQL (in-memory H2 dev profile)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

### Tests
```bash
mvn test
```

Then open <http://localhost:8080>.

---

## Docker deployment (2 containers)

Two services are defined in [`docker-compose.yml`](docker-compose.yml):

- **`idcard-web`** — JDK 25 + Maven + Git + NGINX + OpenSSH. Builds and runs this
  project internally on **8081**; NGINX listens on **8080** and proxies to it.
- **`idcard-mysql`** — MySQL 8 with a named volume `mysql-data`.

| Service | Host port | Container port | Purpose |
| --- | --- | --- | --- |
| web (NGINX → Spring Boot) | **8443** | 8080 → 8081 | Website |
| web (SSH) | **2222** | 22 | SSH (root / `Hello@123`) |
| mysql | **3307** | 3306 | MySQL 8 |

Spring Boot connects to MySQL using the datasource URL supplied via environment
variables in `docker-compose.yml`:

```
jdbc:mysql://mysql:3306/B-PENG_Seyha-db
```

The web container waits for MySQL's health check to pass before starting.

### Build & run
```bash
docker compose up --build
```

### Verify everything
```bash
# list running containers
docker ps

# hit the website through NGINX -> Spring Boot
curl http://localhost:8443

# SSH into the web container (password: Hello@123)
ssh root@localhost -p 2222

# open a MySQL shell (password: Hello@123)
docker exec -it idcard-mysql mysql -uroot -p
```

Inside the MySQL shell:
```sql
SHOW DATABASES;
-- expect to see: B-PENG_Seyha-db
```

### Stop / clean up
```bash
docker compose down            # stop containers
docker compose down -v         # stop and delete the MySQL volume
```

---

## Configuration reference

| Env var | Default | Meaning |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/id_card_manager?...` | JDBC datasource URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | `root` | DB password |
| `APP_BASE_URL` | `http://localhost:8080` | Base URL embedded in QR verification links |
| `PHOTO_DIR` | `uploads/photos` | Local directory for uploaded photos |
