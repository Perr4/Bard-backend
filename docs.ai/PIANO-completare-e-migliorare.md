# Piano — cose da completare e cose da migliore

Regola: **niente iniziative, niente modifiche senza il via libera di Perra.**

---

# PARTE 1 — Cose da completare

## 1.1 `PUT /api/ballerino` — oggi ritorna `null`

**Stato**: `BallerinoService.modificaBallerino()` ha corpo vuoto (`return null`).
Il controller esiste (`@PutMapping("/ballerino")`) ma:
- non ha `@Valid`
- non gestisce il 404
- ritorna sempre `null` con 200

**Cosa manca**:
1. Caricare l'entita' per id (404 se non esiste)
2. Aggiornare i campi (eta, sezione, sesso — **non** userId/anagrafica)
3. Salvare e restituire il DTO (con anagrafica composta)
4. `@Valid` sul controller
5. Verifica: senza token → 401, token falso → 401, valido → 200
6. Verifica: 404 per id inesistente
7. Test

**Domanda aperta**: il corpo della PUT e' `BallerinoDto` (include id, userId,
firstName, lastName...). Va bene, o meglio un DTO dedicato `BallerinoUpdate`
con solo i campi modificabili (eta, sezione, sesso)?

---

## 1.2 `DELETE /api/presenza/{id}` — semantica disallineata

**Stato**:
- **backend**: `DELETE /api/presenza/{id}` si aspetta l'**id della presenza**
- **frontend** (`presenza.ts:26`): `removePresenza(ballerino_id)` manda un
  **ballerino_id** → cancella la presenza sbagliata (o nessuna)

**Due strade**:

**A — correggere il frontend** (consigliata): far passare l'id della presenza.
Serve pero' che il frontend conosca quell'id: l'endpoint `GET /api/presenza/{id}`
restituisce `PresenzaDto` che **ha** l'id. Quindi il front-end puo' usarlo.

**B — cambiare il backend**: `DELETE /api/presenza/ballerino/{ballerinoId}` che
elimina tutte le presenze di un ballerino. Ha senso se il caso d'uso e'
"cancella le presenze di questo ballerino", ma **non** "cancella questa presenza".

**Da decidere**: quale delle due? Dipende dal caso d'uso reale.

---

# PARTE 2 — Cose da migliorare

## 2.1 Gestione degli errori (ProblemDetail)

**Stato attuale**:

| Servizio | Handler | Problema |
|---|---|---|
| `presenza-service` | `ExceptionController` | **classe vuota**, non e' neanche `@ControllerAdvice` |
| `ballerini-service` | `ExceptionController` | `@ControllerAdvice` (manca `Rest`), import `HttpSecurity` inutilizzato, gestisce solo `EntityNotFoundException` |
| `auth-service` | **nessuno** | validazione e credenziali → **403 con `%s` letterale** |
| `gateway` | `RestExceptionController` | gestisce `ExpiredJwtException` → **400** (dovrebbe essere 401) |

**Cosa serve**:
1. `auth-service`: handler per `MethodArgumentNotValidException`,
   `BadCredentialsException`, `ResponseStatusException`
2. `presenza-service`: trasformare `ExceptionController` in `@RestControllerAdvice`
   e gestire la validazione
3. `ballerini-service`: `@RestControllerAdvice` + rimuovere l'import inutile
4. `gateway`: `ExpiredJwtException` → 401 invece di 400
5. Formato uniforme: `ProblemDetail` (RFC 7807)

**Verifica**: chiamare un endpoint con input non valido → errore leggibile,
non `%s`.

---

## 2.2 Test del gateway

**Stato**: `ApiGatewayApplicationTests` fa solo il **context load**. Non ci sono
test su:
- le rotte (`/api/auth/**` → AUTH-SERVICE, ecc.)
- il filtro JWT (401 senza token, 401 token falso, 200 token valido)

**Cosa serve**:
1. Test delle rotte con `WebTestClient`
2. Test del filtro JWT
3. Verifica: 401 senza token, 401 token falso, 200 token valido

---

## 2.3 CORS

**Stato**: configurato in **4 posti**:
- `gateway` (reattivo)
- `auth-service`, `ballerini-service`, `presenza-service` (servlet)

**Problema**: i servizi non sono esposti all'esterno (solo il gateway lo e'),
quindi il loro CORS e' **ridondante** — ma utile se un servizio viene chiamato
direttamente (es. sviluppo).

**Due strade**:
- **A** — lasciare tutto com'e' (il CORS dei servizi copre la chiamata diretta)
- **B** — rimuovere il CORS dai servizi e lasciarlo solo al gateway

**Da decidere**: A o B. Consiglio **A** (piu' permissivo, non rompe nulla).

---

## 2.4 `AGENTS.md`

**Stato**: e' alla **radice** del progetto (`/media/myfiles/SSD/Code/BARD/AGENTS.md`),
non nella repo backend. Contiene la struttura giusta ma va ricontrollato dopo
tutte le modifiche di questa sessione (Docker, compose, `docs.ai/`, healthcheck,
endpoint nuovi, nessun Kafka).

---

# Ordine proposto

1. **1.2** (`DELETE presenza`) — prima, perche' decide la semantica
2. **1.1** (`PUT ballerino`)
3. **2.1** errori
4. **2.2** test gateway
5. **2.3** CORS
6. **2.4** `AGENTS.md`

---

# Da decidere prima di partire

1. `DELETE presenza`: **A** (fix frontend) o **B** (nuovo endpoint backend)?
2. `PUT ballerino`: `BallerinoDto` o un `BallerinoUpdate` dedicato?
3. CORS: **A** (lascia tutto) o **B** (solo gateway)?
4. `AGENTS.md`: lo aggiorno nella repo backend, oppure resta alla radice?
