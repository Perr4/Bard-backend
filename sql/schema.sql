-- =====================================================================
-- BARD backend - schema condiviso (PostgreSQL)
-- Cartella: bard-backend/sql/
--
-- Contiene le tabelle di TUTTE le entita JPA del backend:
--   - presenza-service  : presenza
--   - auth-service      : auth_users, auth_roles, auth_user_roles
--   - ballerini-service : ballerini
--
-- Convenzioni:
--   * UUID generati dal database (gen_random_uuid(), pgcrypto)
--   * vincoli NOT NULL / UNIQUE / CHECK espliciti
--   * ddl-auto: validate (Hibernate verifica, non crea)
--
-- Uso:
--   psql -h <host> -U <user> -d postgres -c "CREATE SCHEMA bard;"
--   psql -h <host> -U <user> -d postgres -f sql/schema.sql
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------
-- presenza-service
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS bard.presenza (
    "presenzaId"  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    date          DATE         NOT NULL,
    ballerino_id  UUID         NOT NULL
);

-- indice per la query findAllByBallerinoId
CREATE INDEX IF NOT EXISTS idx_presenza_ballerino
    ON bard.presenza (ballerino_id);

-- ---------------------------------------------------------------------
-- auth-service
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS bard.auth_roles (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(80)  NOT NULL UNIQUE,
    description  VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bard.auth_users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    data_nascita  DATE,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auth_users_email
    ON bard.auth_users (email);

CREATE TABLE IF NOT EXISTS bard.auth_user_roles (
    user_id  UUID NOT NULL REFERENCES bard.auth_users (id) ON DELETE CASCADE,
    role_id  UUID NOT NULL REFERENCES bard.auth_roles (id)  ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ruoli di default richiesti da AuthController (DEFAULT_ROLE = "STUDENT")
INSERT INTO bard.auth_roles (name, description)
VALUES ('STUDENT', 'Studente')
ON CONFLICT (name) DO NOTHING;

-- ---------------------------------------------------------------------
-- ballerini-service
-- ---------------------------------------------------------------------

-- Il profilo ballerino e' opzionale (1 utente : 0..1 ballerino).
-- Lo user_id e' un riferimento logico a bard.auth_users (DB distinti:
-- non c'e' una FK fisica). L'anagrafica (nome, cognome, data_nascita)
-- appartiene ad auth_users: non qui.
CREATE TABLE IF NOT EXISTS bard.ballerini (
    ballerino_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL UNIQUE,
    sezione       VARCHAR(32),
    eta           INTEGER,
    sesso         VARCHAR(16)
);
