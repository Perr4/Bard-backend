# ROADMAP — BARD

Roadmap del progetto, aggiornata con Perra.
Stato: **in lavorazione**.

Legenda: `[ ]` da fare · `[~]` in corso · `[x]` fatto · `[!]` bloccato

---

## Fase 1 — Fondamenta del backend

- [x] Schema database `sql/schema.sql` (tabelle `presenza`, `auth_users`, `auth_roles`, `auth_user_roles`, `ballerini`)
- [x] Validazione Bean Validation sui DTO + `@Valid` nei controller
- [x] Test DTO
- [x] Context load per tutti i servizi (auth, ballerini, presenza, gateway, eureka)
- [x] Stack unificato: Spring Boot 4.1.1 + Java 21 per tutti i moduli
- [x] Rimosso l'import spurio `jakarta.ws.rs.QueryParam` in `PresenzaRepo`

## Fase 2 — Completamento dei microservizi (in corso)

### auth-service
- [x] Registrazione, login, profilo, emissione JWT
- [x] `POST /api/auth/token` con verifica reale del JWT
- [x] Filtro JWT registrato nella catena di sicurezza
- [ ] Handler `ProblemDetail` per errori di validazione e credenziali

### ballerini-service
- [x] CRUD ballerini + DTO + validazione
- [x] `GET /api/ballerino/{id}` (base)
- [ ] **Completare la dashboard**: includere le presenze nel DTO
- [ ] Implementare `PUT /api/ballerino` (oggi `modificaBallerino` ritorna `null`)

### presenza-service
- [x] CRUD presenze
- [ ] `GET /api/presenza` — tutte le presenze (oggi 405)
- [ ] Correggere `DELETE /api/presenza/{id}`: il frontend passa un `ballerino_id`
- [ ] Handler `ProblemDetail` per `MethodArgumentNotValidException`

### gateway
- [x] Token JWT non valido → 401 (era 500)
- [x] `/api/auth/token` pubblico (introspezione)
- [ ] Test delle rotte e del filtro JWT
- [ ] Verificare la configurazione CORS (duplicata con i servizi)

## Fase 3 — Uscite

- [ ] Definire il modello: entita' `Uscita` (data, luogo, descrizione, partecipanti)
- [ ] Schema: tabella `bard.uscite` + tabella di collegamento ai ballerini
- [ ] CRUD uscite (create, read, delete)
- [ ] Endpoint "tutti i ballerini escono assieme" (un'uscita con tutti i partecipanti)
- [ ] DTO + validazione
- [ ] Test

## Fase 4 — Report ed export

- [ ] **Nuovo `report-service`**: servizio dedicato ai calcoli e agli export
      (i report sono transversali: leggono presenze, uscite e anagrafiche)
- [ ] Endpoint JSON del rendiconto (presenze per mese, per ballerino)
- [ ] Export **Excel** (Apache POI): `GET /api/report/...?format=xlsx`
- [ ] Export **PDF** (se serve)
- [ ] Rendiconto **mensile** (conteggi presenze/uscite)
- [ ] Test

## Fase 5 — Test di integrazione

- [ ] Test che avvia lo stack e verifica login → token → chiamata autenticata
      (i bug trovati non si vedevano dai test unitari)
- [ ] Test del flusso completo: registrazione → presenza → report

## Fase 6 — Osservabilità e operatività

- [x] Healthcheck su tutti i servizi nel compose
- [x] Ordine di avvio: postgres → eureka → auth → ballerini/presenza → gateway
- [ ] Verificare configurazione OTLP/OpenTelemetry
- [ ] Probe di readiness e liveness per k8s

---

## Note

- Priorita' immediata: **chiudere la Fase 2** (dashboard, `GET /api/presenza`,
  bug rimasti), poi attuare il front-end su quelle API.
- Ogni endpoint nuovo o modificato va verificato con tre chiamate:
  **senza token → 401**, **token falso → 401**, **token valido → 200**.
- Le entita' restano separate; si compongono con DTO di lettura, senza tabelle nuove
  (tranne per le uscite, che sono un fatto nuovo).
