# =====================================================================
# BARD backend - spunto di schema H2 (NON usato dai test)
# Cartella: bard-backend/sql/
#
# NOTA: i test NON usano questo file. Lo schema di test lo genera Hibernate
# dalle entita (ddl-auto: create-drop) in uno H2 in memoria.
#
# Questo file e solo uno SPUNTO di riferimento: mostra come sarebbe lo schema
# H2 equivalente a schema.sql, utile in seguito per Docker Compose / Kubernetes.
# Usa funzioni H2 (RANDOM_UUID() al posto di gen_random_uuid()).
# =====================================================================

CREATE SCHEMA IF NOT EXISTS bard;

-- ---------------------------------------------------------------------
-- presenza-service
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS bard.presenza (
    presenzaId  UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    date        DATE         NOT NULL,
    ballerino_id UUID        NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_presenza_ballerino
    ON bard.presenza (ballerino_id);

-- ---------------------------------------------------------------------
-- auth-service
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS bard.auth_roles (
    id           UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    name         VARCHAR(80)  NOT NULL UNIQUE,
    description  VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bard.auth_users (
    id            UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
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

INSERT INTO bard.auth_roles (name, description)
VALUES ('STUDENT', 'Studente');
