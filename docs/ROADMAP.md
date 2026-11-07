# ROADMAP — BARD

Roadmap del progetto, da aggiornare con Perra.
Stato: **bozza iniziale** — le voci sono derivate dallo stato attuale del codice e dalle
dichiarazioni raccolte. Da raffinare insieme.

Legenda: `[ ]` da fare · `[~]` in corso · `[x]` fatto · `[!]` bloccato

---

## Fase 1 — Fondamenta del backend

- [x] Schema database `sql/schema.sql` (tabelle `presenza`, `auth_users`, `auth_roles`, `auth_user_roles`)
- [x] Validazione Bean Validation sui DTO (`PresenzaDto`, `AuthDtos`) + `@Valid` nei controller
- [x] Test DTO (accessori, uguaglianza, vincoli di validazione)
- [x] Context load per `presenza-service` e `ballerini-service`
- [ ] **Context load per `auth-service`** — il servizio più critico non ha ancora un `@SpringBootTest`
- [ ] **Context load per `gateway`** — nessun test presente
- [ ] Allineare le versioni Spring Boot (presenza/ballerini 4.1.1 vs auth/gateway 3.5.0)

## Fase 2 — Completamento dei microservizi

### auth-service
- [x] Registrazione, login, profilo, emissione JWT
- [ ] Gestire `POST /api/auth/token`: oggi risponde sempre `{"valid": true}` senza validare nulla
- [ ] Evento Kafka `chronos.user.registered` (publisher attualmente commentato)
- [ ] Handler `ProblemDetail` per errori di validazione e credenziali

### ballerini-service
- [ ] Entity `Ballerino`, repository, service, controller (CRUD)
- [ ] DTO + validazione
- [ ] Test del repository e del controller

### presenza-service
- [x] CRUD presenze
- [ ] Correggere l'import `jakarta.ws.rs.QueryParam` in `PresenzaRepo` (usare `@Param` di Spring Data)
- [ ] Handler `ProblemDetail` per `MethodArgumentNotValidException` (`ExceptionController` è vuoto)
- [ ] Rimuovere `System.out.println` dal controller e l'import spurio `IModelFilter` nel service
- [ ] Test di integrazione (repository + controller)

### gateway
- [ ] Test delle rotte e del filtro JWT
- [ ] Verificare la configurazione CORS (attualmente duplicata con quella dei servizi)

## Fase 3 — Test di integrazione

- [ ] Dipendenza H2 (scope test) nei pom
- [ ] Variante H2 di `schema.sql` in `src/test/resources` (test ermetici, senza PostgreSQL remoto)
- [ ] `PresenzaRepoIntegrationTest` — save/findById, filtro per `ballerino_id`, delete
- [ ] `PresenzaControllerIntegrationTest` — 4 endpoint + 400 su input non valido
- [ ] `AuthControllerIntegrationTest` — register/login/me, 409 email duplicata, 400 validazione
- [ ] Seed dei ruoli di test (`STUDENT`) prima di ogni test di auth

## Fase 4 — Osservabilità e operatività

- [ ] Verificare configurazione OTLP/OpenTelemetry (metriche, trace, log)
- [ ] Health check / probe di readiness e liveness
- [ ] Allineamento tra `strumenti/Observability` e la configurazione dei servizi

## Fase 5 — Futuro: ampliamento delle presenze

- [ ] **Introduzione di due tipi di presenza**
  - Definire il modello (campo tipo / enum, o entità separate)
  - Impatto su schema, DTO, service e controller
  - Test del nuovo comportamento
  - Valutare la migrazione dello schema esistente

---

## Note

- La priorità immediata è la **Fase 1**: manca solo il context load di `auth-service` e `gateway`
  per avere tutti i servizi verificati almeno all'avvio.
- Ogni feature va sviluppata in modalità **TDD**: prima il test che verifica il comportamento,
  poi l'implementazione, e si controlla che la modifica non rompa il programma.
