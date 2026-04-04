# Klassenbuch API

REST API für das Klassenbuch-System — verwaltet Lernfelder, Dozenten, Lerntage und Unterrichtseinheiten.

Built with **Java 21** · **Spring Boot 3.3** · **Spring Data JPA** · **PostgreSQL**

> **Datenbankquelle:** Die Datenbank wird vom Projekt
> [GrigoreVoda/klassenbuch](https://github.com/GrigoreVoda/klassenbuch) befüllt,
> das Klassenbuch-PDFs parst und die Daten in PostgreSQL speichert.

---

## Welche Installationsvariante passt zu mir?

| Situation | Empfohlene Variante |
|-----------|-------------------|
| Ich habe **keine PostgreSQL** auf meinem Rechner | ✅ **Variante C** — alles in Docker |
| Ich **entwickle** und will schnellen Neustart | ✅ **Variante A** — direkt mit Maven |
| Ich habe schon eine **laufende PostgreSQL** mit Daten | ✅ **Variante B** — nur App in Docker |

---

## Voraussetzungen

| Tool | Variante A | Variante B | Variante C |
|------|-----------|-----------|-----------|
| Java JDK 21 | ✅ | ❌ | ❌ |
| Maven / `./mvnw` | ✅ | ❌ | ❌ |
| PostgreSQL (lokal) | ✅ | ✅ | ❌ |
| Docker + Docker Compose | ❌ | ✅ | ✅ |
| Git | ✅ | ✅ | ✅ |

---

## Variante A — Direkt mit Maven (Entwicklung)

Für Entwickler mit laufender lokaler PostgreSQL. Kein Docker nötig.

### 1. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 2. Datenbank prüfen

```bash
psql -U postgres -d klassenbuch -c "\dt"
```

Du solltest 5 Tabellen sehen: `dozent`, `lernfeld`, `lernfeld_dozent`, `lerntag`, `unterrichtseinheit`.

Falls nicht, Schema anlegen:

```bash
curl -o init_db.sql \
  https://raw.githubusercontent.com/GrigoreVoda/klassenbuch/main/init_db.sql.txt
```

### 3. Passwort konfigurieren

```bash
nano src/main/resources/application-dev.properties
```

Zeile anpassen:

```properties
spring.datasource.password=DEIN_POSTGRES_PASSWORT
```

### 4. Starten

```bash
./mvnw spring-boot:run
```

### 5. Prüfen

```
http://localhost:8080/swagger-ui.html  →  Swagger UI
http://localhost:8080/dozenten         →  JSON-Liste der Dozenten
```

---

## Variante B — Nur App in Docker (bestehende PostgreSQL auf Fedora)

Die App läuft im Container, die PostgreSQL bleibt auf dem Host.

### 1. PostgreSQL für Docker-Zugriff freigeben

```bash
# pg_hba.conf: Docker-Netzwerk erlauben
sudo nano /var/lib/pgsql/data/pg_hba.conf
```

Diese Zeile **vor** dem `127.0.0.1`-Eintrag einfügen:

```
host    klassenbuch    postgres    172.17.0.0/16    md5
```

```bash
# postgresql.conf: auf allen Interfaces lauschen
sudo nano /var/lib/pgsql/data/postgresql.conf
```

Ändern auf:

```
listen_addresses = '*'
```

```bash
# PostgreSQL neu starten und Firewall öffnen:
sudo systemctl restart postgresql
sudo firewall-cmd --add-port=5432/tcp --permanent
sudo firewall-cmd --reload
```

### 2. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 3. App-Container starten

```bash
docker build -t klassenbuch-api:latest .

docker run -d \
  --name klassenbuch-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host-gateway:5432/klassenbuch \
  -e DB_USER=postgres \
  -e DB_PASS=DEIN_PASSWORT \
  --add-host host-gateway:host-gateway \
  klassenbuch-api:latest
```

```bash
# Logs verfolgen:
docker logs -f klassenbuch-api

# Stoppen:
docker stop klassenbuch-api && docker rm klassenbuch-api
```

---

## Variante C — Alles in Docker (Neuinstallation, empfohlen)

**Nur Docker nötig.** PostgreSQL und App starten zusammen mit einem Befehl.
Die Tabellen werden beim ersten Start automatisch angelegt.

### 1. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 2. Schema-Datei herunterladen

Das Datenbankschema aus dem Klassenbuch-Projekt holen:

```bash
curl -o init_db.sql \
  https://raw.githubusercontent.com/GrigoreVoda/klassenbuch/main/init_db.sql.txt
```

### 3. Passwort festlegen

```bash
cp .env.template .env
nano .env
```

Inhalt von `.env`:

```env
DB_PASS=dein_gewaehltes_passwort
```

> `.env` steht in `.gitignore` — wird nie in Git gespeichert.

### 4. Alles starten

```bash
docker compose up --build
```

Docker macht automatisch:
1. PostgreSQL-Container starten
2. Datenbank `klassenbuch` anlegen
3. Alle Tabellen aus `init_db.sql` anlegen
4. App bauen und starten (wartet bis DB bereit ist)

Beim ersten Start dauert es 1–2 Minuten wegen des Maven-Downloads.

### 5. Prüfen

```
http://localhost:8080/swagger-ui.html  →  Swagger UI
http://localhost:8080/dozenten         →  leere Liste [] (noch keine Daten)
```

### 6. Daten importieren (optional)

Um Daten aus Klassenbuch-PDFs zu importieren:

```bash
# Im klassenbuch-Projekt (PDF-Parser), mit DB auf Port 5432:
python klassenbuch_pdf_parsing.py
```

### Nützliche Befehle für Variante C

```bash
# Starten im Hintergrund:
docker compose up -d --build

# Logs der App:
docker compose logs -f app

# Logs der Datenbank:
docker compose logs -f db

# Status prüfen:
docker compose ps

# Stoppen (Daten bleiben erhalten):
docker compose down

# Kompletter Reset — Daten werden GELÖSCHT:
docker compose down -v
```

---

## API Endpunkte

Vollständige interaktive Dokumentation: **http://localhost:8080/swagger-ui.html**

### Dozenten `/dozenten`

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| `GET` | `/dozenten` | Alle Dozenten |
| `GET` | `/dozenten/{id}` | Dozent nach ID |
| `POST` | `/dozenten` | Neu anlegen |
| `PUT` | `/dozenten/{id}` | Aktualisieren |
| `DELETE` | `/dozenten/{id}` | Löschen |

### Lernfelder `/lernfelder`

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| `GET` | `/lernfelder` | Alle Lernfelder |
| `GET` | `/lernfelder/{id}` | Nach ID (z.B. `LF1`) |
| `POST` | `/lernfelder` | Neu anlegen |
| `PUT` | `/lernfelder/{id}` | Aktualisieren |
| `DELETE` | `/lernfelder/{id}` | Löschen |
| `GET` | `/lernfelder/{id}/dozenten` | Zugewiesene Dozenten |
| `POST` | `/lernfelder/{id}/dozenten/{did}` | Dozenten zuweisen |
| `DELETE` | `/lernfelder/{id}/dozenten/{did}` | Dozenten entfernen |

### Lerntage `/lerntage`

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| `GET` | `/lerntage` | Alle (Filter: `?lernfeld_id=LF1`) |
| `GET` | `/lerntage/{id}` | Mit allen Unterrichtseinheiten |
| `GET` | `/lerntage/search?q=...` | Suche in Unterrichtsinhalten |
| `POST` | `/lerntage` | Neu anlegen |
| `PUT` | `/lerntage/{id}` | Aktualisieren |
| `DELETE` | `/lerntage/{id}` | Löschen (kaskadiert Einheiten) |

### Unterrichtseinheiten `/lerntage/{id}/einheiten`

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| `GET` | `/lerntage/{id}/einheiten` | Alle Einheiten eines Lerntags |
| `GET` | `/lerntage/{id}/einheiten/{eid}` | Einzelne Einheit |
| `POST` | `/lerntage/{id}/einheiten` | Neue Einheit (Stunde 1–9) |
| `PUT` | `/lerntage/{id}/einheiten/{eid}` | Aktualisieren |
| `DELETE` | `/lerntage/{id}/einheiten/{eid}` | Löschen |

### Fehlerantworten

Alle Fehler liefern einheitliches JSON:

```json
{
  "timestamp": "2024-09-04T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Dozent not found: id=99"
}
```

Validierungsfehler:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": ["vorname: vorname must not be blank"]
}
```

---

## Projektstruktur

```
klassenbuch-api/
├── src/main/java/com/klassenbuch/api/
│   ├── controller/     ← HTTP-Schicht (@RestController)
│   ├── service/        ← Business-Logik (@Service)
│   ├── repository/     ← Datenbankzugriff (Spring Data)
│   ├── entity/         ← JPA-Entities (Tabellen-Mapping)
│   ├── dto/            ← Request/Response-Objekte
│   └── exception/      ← Fehlerbehandlung
├── src/main/resources/
│   ├── application.properties          ← Gemeinsame Einstellungen
│   ├── application-dev.properties      ← Entwicklung (lokale DB)
│   └── application-prod.properties     ← Produktion (Env-Variablen)
├── Dockerfile
├── docker-compose.yml
└── .env.template
```

---

## Tests ausführen

```bash
# Alle Tests (kein Server oder DB nötig):
./mvnw test
```

---

## Verwandte Projekte

- **[GrigoreVoda/klassenbuch](https://github.com/GrigoreVoda/klassenbuch)** —
  Parst Klassenbuch-PDFs und befüllt die PostgreSQL-Datenbank.
