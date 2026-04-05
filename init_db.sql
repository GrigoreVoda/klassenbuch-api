-- ============================================================
-- Klassenbuch Datenbankschema
-- Automatisch ausgeführt beim ersten Docker-Start
-- ============================================================

CREATE TABLE IF NOT EXISTS lernfeld (
    lernfeld_id VARCHAR(10)  PRIMARY KEY,
    titel       VARCHAR(255) NOT NULL,
    start_datum DATE,
    end_datum   DATE
);

CREATE TABLE IF NOT EXISTS dozent (
    dozent_id SERIAL       PRIMARY KEY,
    vorname   VARCHAR(50)  NOT NULL,
    nachname  VARCHAR(50)  NOT NULL
);

CREATE TABLE IF NOT EXISTS lernfeld_dozent (
    lernfeld_id VARCHAR(10) REFERENCES lernfeld(lernfeld_id) ON DELETE CASCADE,
    dozent_id   INT         REFERENCES dozent(dozent_id)     ON DELETE CASCADE,
    PRIMARY KEY (lernfeld_id, dozent_id)
);

CREATE TABLE IF NOT EXISTS lerntag (
    lerntag_id  SERIAL      PRIMARY KEY,
    datum       DATE        NOT NULL UNIQUE,
    lernfeld_id VARCHAR(10) REFERENCES lernfeld(lernfeld_id) ON DELETE SET NULL,
    dozent_id   INT         REFERENCES dozent(dozent_id)     ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS unterrichtseinheit (
    einheit_id SERIAL PRIMARY KEY,
    lerntag_id INT    NOT NULL REFERENCES lerntag(lerntag_id) ON DELETE CASCADE,
    stunde     INT    CHECK (stunde BETWEEN 1 AND 9),
    inhalt     TEXT,
    UNIQUE (lerntag_id, stunde)
);

-- Indexes für häufige Abfragen
CREATE INDEX IF NOT EXISTS idx_lerntag_datum    ON lerntag(datum);
CREATE INDEX IF NOT EXISTS idx_lerntag_lernfeld ON lerntag(lernfeld_id);
CREATE INDEX IF NOT EXISTS idx_einheit_lerntag  ON unterrichtseinheit(lerntag_id);
