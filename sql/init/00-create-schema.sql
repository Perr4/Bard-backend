-- Crea lo schema condiviso del backend BARD.
-- Montato in /docker-entrypoint-initdb.d/ da docker-compose.yml (eseguito prima di schema.sql).
CREATE SCHEMA IF NOT EXISTS bard;
