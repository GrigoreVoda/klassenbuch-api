# Klassenbuch API

REST API für das Klassenbuch-System — verwaltet Lernfelder, Dozenten, Lerntage und Unterrichtseinheiten.

Built with **Java 21** · **Spring Boot 3.3** · **Spring Data JPA** · **PostgreSQL**

> Die Datenbank wird vom Projekt
> [GrigoreVoda/klassenbuch](https://github.com/GrigoreVoda/klassenbuch) befüllt,
> das Klassenbuch-PDFs parst und die Daten in PostgreSQL speichert.

---

## Welche Variante passt zu mir?

| Ich habe... | Variante |
|-------------|----------|
| Keine PostgreSQL auf meinem Rechner | ✅ **Variante A** — App + DB in Docker |
| Schon eine laufende PostgreSQL mit Daten (z.B. Fedora) | ✅ **Variante B** — nur App in Docker |
| IntelliJ und will direkt entwickeln | ✅ **Variante C** — Maven direkt |

---

## Variante A — App + DB in Docker (Neuinstallation)

**Einzige Voraussetzung: Docker**

### 1. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 2. Passwort und Benutzer festlegen

Im Projekt-Root (gleiche Ebene wie `docker-compose.full.yml`):
```bash
touch .env
nano .env
```

Inhalt von `.env` ausfüllen:
```env
DB_URL=jdbc:postgresql://localhost:5432/klassenbuch
DB_USER=
DB_PASS=dein_passwort
```

### 3. Starten

```bash
docker compose -f docker-compose.full.yml up --build -d
```

Beim ersten Start (~2 Min) passiert automatisch:
- PostgreSQL Container starten
- Datenbank `klassenbuch` anlegen
- Alle Tabellen aus `init_db.sql` anlegen
- App bauen und starten (wartet bis DB bereit ist)

### 4. Prüfen

```bash
curl http://localhost:8080/lernfelder
# → [] (leer, noch keine Daten)
```

Swagger UI: **http://localhost:8080/swagger-ui.html**

### 5. Daten importieren (optional)

**Option A — Neue Daten aus PDFs importieren** (frische Installation):

Siehe [klassenbuch projekt](https://github.com/GrigoreVoda/klassenbuch) für
Installationsanleitung und Nutzung des PDF-Parsers.
```bash
# Im klassenbuch PDF-Parser Projekt:
python klassenbuch_pdf_parsing.py
# Schreibt direkt in die DB auf Port 5432
```

**Option B — Bestehenden Dump importieren** (wenn du schon Daten hast):
```bash
# Dump von alter PostgreSQL erstellen (auf dem alten Server):
pg_dump -U postgres -d klassenbuch -F c -f klassenbuch_backup.dump

# Dump in Docker importieren:
docker compose -f docker-compose.full.yml exec -T db \
  pg_restore -U postgres -d klassenbuch --no-owner \
  < klassenbuch_backup.dump

# Prüfen ob Daten da sind:
docker compose -f docker-compose.full.yml exec db \
  psql -U postgres -d klassenbuch \
  -c "SELECT COUNT(*) FROM lerntag;"
```

> Falls du Fehler wie `relation already exists` siehst — das ist normal.
> Die Tabellen wurden bereits von `init_db.sql` angelegt.
> Die Daten werden trotzdem importiert.



### Befehle

```bash
# Status prüfen:
docker compose -f docker-compose.full.yml ps

# Logs App:
docker compose -f docker-compose.full.yml logs -f app

# Logs DB:
docker compose -f docker-compose.full.yml logs -f db

# Mit sql konatainer interaktiv verbinden
docker compose -f docker-compose.full.yml exec -it db psql -U postgres

# Dump von Docker container
docker compose -f docker-compose.full.yml exec -T db \
  pg_dump -U postgres -d klassenbuch -F c \
  > /var/backups/klassenbuch_$(date +\%F).dump # gib deine path

oder von adere host durch SSH
ssh user@remote-host "docker compose -f docker-compose.full.yml exec -T db pg_dump -U postgres -d klassenbuch -F c" > ./local_klassenbuch$(date +%F).dump

# Dump in Docker importieren:
docker compose -f docker-compose.full.yml exec -T db \
  pg_restore -U postgres -d klassenbuch --no-owner \
  < klassenbuch_backup.dump

# Stoppen (Daten bleiben erhalten):
docker compose -f docker-compose.full.yml down

# Alles löschen inkl. Datenbank — kompletter Reset:
docker compose -f docker-compose.full.yml down -v
```

---

## Variante B — Nur App in Docker (bestehende PostgreSQL)

Die App läuft im Container, PostgreSQL bleibt auf dem Host.

### 1. PostgreSQL für Docker-Zugriff freigeben

Nur nötig wenn die DB auf einem anderen Rechner oder Fedora-Server läuft.
Wenn DB und Docker auf demselben Rechner sind → direkt zu Schritt 2.

```bash
# pg_hba.conf: Docker-Netzwerk erlauben
sudo nano /var/lib/pgsql/data/pg_hba.conf
```

Diese Zeile vor dem `127.0.0.1`-Eintrag einfügen:

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
# Neu starten und Firewall öffnen:
sudo systemctl restart postgresql
sudo firewall-cmd --add-port=5432/tcp --permanent
sudo firewall-cmd --reload
```

### 2. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 3. Passwort setzen und starten

```bash
echo "DB_PASS=dein_postgres_passwort" > .env
docker compose up --build -d
```

Die App verbindet sich automatisch über `host-gateway:5432` mit der lokalen PostgreSQL.

### Befehle

```bash
# Logs:
docker compose logs -f app

# Stoppen:
docker compose down

# Neu starten nach Code-Änderung:
docker compose up --build -d
```

---

## Variante C — Direkt mit Maven (Entwicklung)

**Voraussetzungen: Java 21, Maven, laufende PostgreSQL**

### 1. Repository klonen

```bash
git clone https://github.com/GrigoreVoda/klassenbuch-api.git
cd klassenbuch-api
```

### 2. Datenbank prüfen

```bash
psql -U postgres -d klassenbuch -c "\dt"
```

5 Tabellen müssen vorhanden sein: `dozent`, `lernfeld`, `lernfeld_dozent`,
`lerntag`, `unterrichtseinheit`. Falls nicht:

```bash
psql -U postgres -d klassenbuch -f init_db.sql
```

### 3. Passwort konfigurieren

```bash
nano src/main/resources/application-dev.properties
```

```properties
spring.datasource.password=DEIN_POSTGRES_PASSWORT
```

### 4. Starten

```bash
./mvnw spring-boot:run
```

---

## Code ändern und neu deployen

### Java-Code geändert (normal)

```bash
# Lokal:
git add .
git commit -m "feat: meine Änderung"
git push

# Auf der anderen Maschine:
git pull
docker compose up --build -d          # Variante B
# oder
docker compose -f docker-compose.full.yml up --build -d   # Variante A
```

### Datenbankschema geändert (neue Spalte, neue Tabelle)

Spring startet nicht wenn Entity und DB nicht übereinstimmen.
Zuerst die DB anpassen, dann die App neu starten:

```bash
# Variante A (DB im Docker):
docker compose -f docker-compose.full.yml exec db \
  psql -U postgres -d klassenbuch \
  -c "ALTER TABLE lerntag ADD COLUMN raum VARCHAR(20);"

# Variante B/C (DB auf dem Host):
psql -U postgres -d klassenbuch \
  -c "ALTER TABLE lerntag ADD COLUMN raum VARCHAR(20);"

# Danach neu starten:
git pull
docker compose up --build -d
```

---

## API Endpunkte

Vollständige Dokumentation: **http://localhost:8080/swagger-ui.html**

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

## Dateien im Projekt

| Datei | Zweck |
|-------|-------|
| `docker-compose.yml` | Nur App in Docker (Variante B — eigene DB) |
| `docker-compose.full.yml` | App + DB in Docker (Variante A — Neuinstallation) |
| `Dockerfile` | Multi-Stage Build: JDK zum Bauen, JRE zum Ausführen |
| `init_db.sql` | Datenbankschema — wird beim ersten Start automatisch ausgeführt |
| `.env.template` | Vorlage für Passwörter — als `.env` kopieren und ausfüllen |
| `application-dev.properties` | Entwicklung: lokale DB, SQL-Logging aktiviert |
| `application-prod.properties` | Produktion: Zugangsdaten aus Umgebungsvariablen |

---

## Tests ausführen

```bash
# Kein laufender Server oder DB nötig:
./mvnw test
```

---

## Verwandte Projekte

- **[GrigoreVoda/klassenbuch](https://github.com/GrigoreVoda/klassenbuch)** —
  Parst Klassenbuch-PDFs und befüllt die PostgreSQL-Datenbank.
