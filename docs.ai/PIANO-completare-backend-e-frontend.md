# Piano — Completare il backend e attuare il front-end

Obiettivo: chiudere le cose iniziate (dashboard ballerino, `GET /api/presenza`,
composizione batch), poi farle usare dal front-end.

---

## Task 1 — Dashboard del ballerino: includere le presenze

**Dove**: `ballerini-service`

1. Creare il DTO `BallerinoDashboardDto` (id, firstName, lastName, eta, sezione, `List<PresenzaDto>`)
2. In `BalleriniRepo`, aggiungere `findById` con `@EntityGraph(attributePaths = "presenze")`
3. In `BalleriniService`, aggiungere `getDashboard(id)`:
   - carica il ballerino (con presenze)
   - compone l'anagrafica via `AuthServiceClient` (come fa gia' `toDto`)
   - restituisce il DTO
4. In `BallerinoController`, aggiungere `GET /api/ballerino/{id}/dashboard`
5. Verifica: senza token → 401, token falso → 401, valido → 200
6. Verifica: il DTO contiene l'anagrafica e la lista presenze
7. Test: `BallerinoServiceTest` con `AuthServiceClient` mockato
8. Commit

**Attenzione**: `PresenzaDto` e' in `presenza-service`, non in `bard-common`. Il DTO
dashboard dovra' definire i propri campi (oppure spostare `PresenzaDto` in `bard-common`).

---

## Task 2 — `GET /api/presenza` (tutte le presenze)

**Dove**: `presenza-service`

1. `PresenzaRepo`: aggiungere `List<Presenza> findAll()` (gia' ereditato da JpaRepository)
2. `PresenzaService`: aggiungere `getAll()`
3. `PresenzaController`: aggiungere `GET /api/presenza`
4. Verifica: senza token → 401, token falso → 401, valido → 200 + array
5. Test del service
6. Commit

---

## Task 3 — Composizione batch (evita il N+1)

**Dove**: `auth-service` + `presenza-service`

1. `auth-service`: `POST /api/auth/internal/users/batch` con lista di UUID
   - ritorna la lista di anagrafiche (anche parziale se qualche utente non esiste)
   - resta **permitAll** (endpoint interno, non esposto dal gateway)
2. Verifica: senza token → 200 (e' interno), con lista di id → anagrafiche
3. `presenza-service`: `AuthServiceClient` con `findAnagrafiche(List<UUID>)`
4. `PresenzaService.getAllConPersona()`: una chiamata batch invece di N chiamate
5. `PresenzaController`: `GET /api/presenza/con-persona`
6. Verifica: senza token → 401, token falso → 401, valido → 200
7. Verifica: il numero di chiamate HTTP e' 1, non N (log o contatore)
8. Test con mock
9. Commit

---

## Task 4 — Fix `DELETE /api/presenza/{id}`

**Dove**: `presenza-service` + front-end

1. Chiarire la semantica: l'id e' della **presenza**, non del ballerino
2. Correggere il front-end (`presenza.ts`) per passare l'id della presenza
3. Verifica: senza token → 401, token falso → 401, valido → 200
4. Verifica: cancella la presenza giusta (non un'altra)
5. Test
6. Commit

---

## Task 5 — Implementare `PUT /api/ballerino`

**Dove**: `ballerini-service`

1. `BallerinoService.modificaBallerino`: caricare l'entita', aggiornare i campi, salvare
2. Verifica: senza token → 401, token falso → 401, valido → 200
3. Verifica: 404 se il ballerino non esiste
4. Test
5. Commit

---

## Task 6 — Front-end: dashboard del ballerino

**Dove**: `bard-front/src/app/User/user-by-id/`

1. Implementare il componente (oggi e' uno stub vuoto)
2. Chiamare `GET /api/ballerino/{id}/dashboard`
3. Mostrare anagrafica + tabella delle presenze
4. Test del componente (mock di HttpClient)
5. Build + test
6. Commit

---

## Task 7 — Front-end: pagina presenze (tutte)

1. Servizio: metodo `getAll()` verso `GET /api/presenza`
2. Componente che mostra la lista completa
3. Collegare la voce "Presenze" della navbar (oggi punta a `/dashboard`)
4. Test + build
5. Commit

---

## Task 8 — Documentazione

1. Aggiornare `README.md` e `docs/README.md` con i nuovi endpoint
2. Aggiornare `docs/ROADMAP.md`
3. Commit

---

## Ordine

1 → 2 → 3 (backend, le API di cui il frontend ha bisogno)
4 → 5 (bug rimasti)
6 → 7 (front-end sulle API nuove)
8 (doc)

---

## Criterio di completamento

- Ogni endpoint: **senza token → 401, token falso → 401, token valido → 200**
- Test backend verdi
- Front-end: build verde + test verdi
- Niente N+1 (verificato dal contatore)
