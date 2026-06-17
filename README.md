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

## Docker deployment (3 containers)

Three services are defined in [`docker-compose.yml`](docker-compose.yml):

- **`idcard-web`** — JDK 25 + Maven + Git + NGINX + OpenSSH. Builds and runs this
  project internally on **8081**; NGINX listens on **8080** and proxies to it.
- **`idcard-mysql`** — MySQL 8 with a named volume `mysql-data`.
- **`idcard-phpmyadmin`** — phpMyAdmin for browser-based MySQL management.

| Service | Host port | Container port | Purpose |
| --- | --- | --- | --- |
| web (NGINX → Spring Boot) | **8443** | 8080 → 8081 | Website |
| web (SSH) | **2222** | 22 | SSH (root / `Hello@123`) |
| mysql | **3307** | 3306 | MySQL 8 |
| phpMyAdmin | **8082** | 80 | MySQL admin UI |

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

### phpMyAdmin

Open phpMyAdmin on the Docker host:

```text
http://localhost:8082
```

From another PC on the same network, use the host machine's LAN IP:

```text
http://<server-ip>:8082
```

Login:

| Field | Value |
| --- | --- |
| Server | `mysql` |
| Username | `root` |
| Password | `Hello@123` |

If a remote PC cannot open phpMyAdmin, allow inbound TCP port `8082` in the
host firewall.

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

## Databases per environment

| Environment | Database | Configured in |
| --- | --- | --- |
| Production | **MySQL 8** | `application.properties` (`DB_URL` env) |
| Tests | **SQLite** | `src/test/resources/application.properties` |
| `h2` dev profile | H2 (in-memory) | `application-h2.properties` |

Tests run against a SQLite file DB (`target/test-idcard.db`) via the
`hibernate-community-dialects` SQLite dialect, so `mvn test` needs no MySQL.

---

## Ansible deployment (CHOICE_A = web server)

[`ansible/deploy.yml`](ansible/deploy.yml) logs in to the **web container** over SSH
(host port `2222`, root / `Hello@123`) and:

1. `git pull`s the latest source (hard reset first, so local changes never block the pull);
2. builds with Maven;
3. runs the test suite against **SQLite**;
4. backs up the **MySQL** production database with `mysqldump`.

```bash
cd ansible
ansible-playbook deploy.yml
```

The backup is written inside the container at `/opt/backups/B-PENG_Seyha-db-<timestamp>.sql`.

### Screenshots to submit (Ansible step)

1. **The playbook file** — `ansible/deploy.yml` open in the editor.
2. **`ansible-playbook deploy.yml` output** — the full run showing each task and the
   final `PLAY RECAP` with `failed=0`, including the "Deployment report" lines:
   - `Maven build: SUCCESS`
   - `Tests: ... Tests run: 5, Failures: 0`
   - `Backup file: ... /opt/backups/B-PENG_Seyha-db-<timestamp>.sql`
3. **SQLite test proof** — in the run output, the "Run the test suite against the SQLite
   test database" task (the test summary shows `Tests run: 5, Failures: 0`).
4. **The backup exists** — `docker exec idcard-web ls -lh /opt/backups/` showing the
   `.sql` file.
5. *(optional)* **Backup is a real dump** —
   `docker exec idcard-web sh -c 'grep "CREATE TABLE" /opt/backups/*.sql'`
   showing the `profiles` and `templates` tables.

> Prerequisite: the stack must be running (`docker compose up -d`) and your latest
> commit pushed to GitHub, since the playbook does a `git pull` from the repo.

---

## Jenkins CI/CD ([Jenkinsfile](Jenkinsfile))

A declarative pipeline drives continuous integration and deployment:

1. **Poll SCM every 5 minutes** (`pollSCM('H/5 * * * *')`) — builds on new Git commits.
2. **Build & test** — `mvnw clean package` then `mvnw test` (against the SQLite test DB).
3. **Email on failure** — notifies the **developer who committed** the breaking change
   (`culprits()`) and **CCs `srengty@gmail.com`**.
4. **Deploy on success** — runs the Ansible playbook (`ansible/deploy.yml`) to the web server.

### One-time Jenkins setup
1. **New Item → Pipeline**.
2. Under *Pipeline*, choose **"Pipeline script from SCM"**, select **Git**, set the repo URL
   (`https://github.com/pengseyha/Final-Exam.git`) and **Script Path** `Jenkinsfile`.
3. Tick **"Poll SCM"** (the schedule comes from the Jenkinsfile trigger).
4. Configure **Manage Jenkins → System → Extended E-mail Notification** (SMTP host, credentials,
   default *From*) so failure emails can be sent.
5. Ensure the agent has **JDK 25, Git, and Ansible**, and can SSH to the web server on port 2222.

---

## GitHub Actions CI/CD ([.github/workflows/ci.yml](.github/workflows/ci.yml))

Runs on **every push**:

1. **Build & test** — JDK 25, `mvnw clean package` (tests use the SQLite test DB).
2. **Email on failure** — emails the **developer who committed** (HEAD commit author)
   and **CCs `srengty@gmail.com`** via SMTP.
3. **Deploy on success** — a separate `deploy` job (`needs: build-and-test`) runs the
   Ansible playbook against the web server.

### Required repository secrets (Settings → Secrets and variables → Actions)
| Secret | Purpose |
| --- | --- |
| `SMTP_SERVER`, `SMTP_PORT` | SMTP host/port for failure emails (e.g. `smtp.gmail.com`, `465`) |
| `SMTP_USERNAME`, `SMTP_PASSWORD` | SMTP login (for Gmail use an App Password) |

### Self-hosted runner for deploy
`build-and-test` runs on a GitHub-cloud runner, but **`deploy` runs on a self-hosted
runner** because the web server is the local Docker container (`127.0.0.1:2222`), which
the cloud cannot reach. Register a runner on the web-server host:

```
Repo → Settings → Actions → Runners → New self-hosted runner  (follow the shown commands)
./run.sh            # keep it running; it picks up the deploy job
```
The host already has `ansible` + `sshpass`, and the playbook's inventory targets
`127.0.0.1:2222`, so the deploy job just runs `ansible-playbook deploy.yml`.

---

## Configuration reference

| Env var | Default | Meaning |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/id_card_manager?...` | JDBC datasource URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | `root` | DB password |
| `APP_BASE_URL` | `http://localhost:8080` | Base URL embedded in QR verification links |
| `PHOTO_DIR` | `uploads/photos` | Local directory for uploaded photos |
