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
| Ich habe **keine PostgreSQL** auf meinem Rechner | ✅ **Variante A** — alles in Docker |
| Ich habe schon eine **laufende PostgreSQL** mit Daten | ✅ **Variante B** — nur App in Docker |
| Ich **entwickle** und will schnellen Neustart ohne Docker | ✅ **Variante C** — direkt mit Maven |

---

## Voraussetzungen

| Tool | Variante A | Variante B | Variante C |
|------|-----------|-----------|-----------|
| Docker + Docker Compose | ✅ | ✅ | ❌ |
| PostgreSQL (lokal laufend) | ❌ | ✅ | ✅ |
| Java JDK 21 | ❌ | ❌ | ✅ |
| Git | ✅ | ✅ | ✅ |

---

## Variante A — Alles in Docker (empfohlen für Neuinstallation)

**Nur Docker muss installiert sein.** PostgreSQL und App starten zusammen mit einem Befehl.
Tabellen werden beim ersten Start automatisch angelegt.

### 1. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 2. Schema-Datei herunterladen

```bash
curl -o init_db.sql \
  https://raw.githubusercontent.com/GrigoreVoda/klassenbuch/main/init_db.sql.txt
```

### 3. Passwort festlegen

```bash
echo "DB_PASS=dein_gewaehltes_passwort" > .env
```

> `.env` steht in `.gitignore` — wird nie in Git gespeichert.

### 4. Alles starten

```bash
docker compose up --build -d
```

Docker erledigt automatisch:
- PostgreSQL-Container starten
- Datenbank `klassenbuch` anlegen
- Alle Tabellen aus `init_db.sql` anlegen
- App bauen (Java + Maven im Container) und starten
- App wartet bis die DB bereit ist

Beim **ersten Start** dauert es 2–3 Minuten wegen des Maven-Downloads.

### 5. Prüfen

```bash
curl http://localhost:8080/dozenten
# → [] (leere Liste, noch keine Daten)
```

Oder im Browser: **http://localhost:8080/swagger-ui.html** → interaktive API-Dokumentation.

### 6. Daten importieren (optional)

```bash
# Im klassenbuch-Projekt (PDF-Parser):
python klassenbuch_pdf_parsing.py
# Der Parser schreibt direkt in die DB auf Port 5432.
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

Zeile ändern auf:

```
listen_addresses = '*'
```

```bash
# PostgreSQL neu starten und Firewall öffnen:
sudo systemctl restart postgresql
sudo firewall-cmd --add-port=5432/tcp --permanent
sudo firewall-cmd --reload
```

### 2. docker-compose.yml anpassen

Die `docker-compose.yml` enthält standardmäßig einen `db`-Service.
Für Variante B (eigene DB) die Datei so verwenden — nur den `app`-Service starten:

```bash
docker compose up --build -d app
```

Der `app`-Service verbindet sich über `host-gateway:5432` mit der lokalen PostgreSQL.

### 3. Passwort setzen und starten

```bash
echo "DB_PASS=dein_postgres_passwort" > .env
docker compose up --build -d app
```

---

## Variante C — Direkt mit Maven (Entwicklung)

Für schnelle Entwicklungszyklen ohne Docker.

### 1. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 2. Datenbank prüfen

```bash
psql -U postgres -d klassenbuch -c "\dt"
```

Du solltest 5 Tabellen sehen. Falls nicht:

```bash
curl -o init_db.sql \
  https://raw.githubusercontent.com/GrigoreVoda/klassenbuch/main/init_db.sql.txt
psql -U postgres -d klassenbuch -f init_db.sql
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

---

## Code ändern und neu deployen

### Nur Java-Code geändert (normal)

```bash
# Lokal:
git add .
git commit -m "feat: deine Änderung"
git push

# Auf der anderen Maschine:
git pull
docker compose up --build -d
```

`--build` baut das App-Image neu. Die Datenbank wird nicht angefasst.

### Neue Dependency in pom.xml hinzugefügt

Gleicher Prozess — `--build` reicht. Docker erkennt die geänderte `pom.xml`
und lädt neue JARs herunter.

### Datenbankschema geändert (neue Spalte, neue Tabelle)

Spring startet nicht wenn Entity und DB-Schema nicht übereinstimmen
(`ddl-auto=validate`). Deshalb muss die DB manuell angepasst werden:

```bash
# Variante A (DB in Docker):
docker compose exec db psql -U postgres -d klassenbuch \
  -c "ALTER TABLE lerntag ADD COLUMN raum VARCHAR(20);"

# Variante B/C (DB auf Host):
psql -U postgres -d klassenbuch \
  -c "ALTER TABLE lerntag ADD COLUMN raum VARCHAR(20);"

# Danach App neu starten:
git pull
docker compose up --build -d
```

---

## Nützliche Befehle

```bash
# Status aller Container:
docker compose ps

# Logs der App verfolgen:
docker compose logs -f app

# Logs der Datenbank verfolgen:
docker compose logs -f db

# App neu starten (ohne Rebuild):
docker compose restart app

# Alles stoppen (Daten bleiben erhalten):
docker compose down

# Komplett zurücksetzen — ALLE DATEN WERDEN GELÖSCHT:
docker compose down -v
docker compose up --build -d
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

```json
{
  "timestamp": "2024-09-04T10:15:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Dozent not found: id=99"
}
```

Validierungsfehler enthalten alle fehlerhaften Felder:

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
├── Dockerfile                          ← Multi-Stage Build
├── docker-compose.yml                  ← App + PostgreSQL
└── .env.template                       ← Vorlage für Passwörter
```

---

## Tests ausführen

```bash
# Alle Tests (kein laufender Server oder DB nötig):
./mvnw test
```

---

## Verwandte Projekte

- **[GrigoreVoda/klassenbuch](https://github.com/GrigoreVoda/klassenbuch)** —
  Parst Klassenbuch-PDFs und befüllt die PostgreSQL-Datenbank.
