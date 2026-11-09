# Bug aperti — da rivedere

Bug emersi durante le verifiche. Quelli marcati **risolto** sono stati corretti e
verificati in Docker; quelli **aperto** sono ancora da lavorare.

---

## Risolto

### 1. `auth-service`: `JwtAuthenticationFilter` non registrato ✅
- **Sintomo**: `GET /api/auth/me` → **500** `NullPointerException: principal is null`
- **Causa**: il filtro esisteva come `@Component` ma non era mai aggiunto alla catena
  di sicurezza in `SecurityConfig` (che aveva `.anyRequest().permitAll()`)
- **Correzione**: iniettato il filtro e aggiunto con
  `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`
- **Verificato**: `/api/auth/me` → **200** con profilo utente

### 2. `ballerini-service`: nome servizio disallineato ✅
- **Sintomo**: `GET /api/ballerino` → **503** `No servers available for service`
- **Causa**: `spring.application.name: learning-service`, ma il gateway instrada su
  `lb://ballerini-service`
- **Correzione**: `spring.application.name: ballerini-service`
- **Verificato**: registrato come `BALLERINI-SERVICE`, `/api/ballerino` → **200**

---

## Aperto

### 3. `presenza-service`: nessun endpoint `GET /api/presenza` (senza id)
- **Sintomo**: `GET /api/presenza` → **405 Method Not Allowed**
- **Dettaglio**: il controller espone solo `GET /api/presenza/{id}` (presenze di un
  ballerino). Non esiste una lista completa.
- **Domanda**: serve `GET /api/presenza` (tutte le presenze) o è voluto?

### 4. `presenza-service`: `DELETE /api/presenza/{id}` usa l'id della presenza
- **Nota**: il frontend (`presenza.ts`) chiama `removePresenza(ballerino_id)` passando
  un **ballerino_id**, ma l'endpoint si aspetta un **presenza_id**.
- **Rischio**: cancella la presenza sbagliata (o nessuna).
- **Da decidere**: correggere il frontend o cambiare la semantica dell'endpoint.

### 5. `auth-service`: `POST /api/auth/token` non valida nulla
- **Sintomo**: risponde sempre `{"valid": true}` senza verificare il token
- **Impatto**: il `authGuard` del frontend si affida a questo endpoint per capire se
  l'utente è loggato → **sempre true**, anche con token scaduto
- **Nota**: il gateway valida il JWT davvero, quindi le API sono protette; il problema
  è solo il guard del frontend

### 6. `auth-service`: `SecurityConfig` ha `.anyRequest().permitAll()`
- **Nota**: tutti gli endpoint sono pubblici a livello di auth-service. La protezione
  reale è al gateway. Va confermato se è voluto (architettura "trust the gateway")
  o se auth-service deve proteggere anche i propri endpoint.

### 7. `ballerini-service`: `PUT /api/ballerino` non implementato
- **Sintomo**: `modificaBallerino()` ritorna `null`
- **File**: `BallerinoService.java`

### 8. Frontend: bug segnalati in precedenza (non ancora corretti)
- `nav-bar.html`: `routerLink="/presenze/{{userId}}"` — l'interpolazione in `routerLink`
  non funziona (genera un link letterale)
- `persona-service.ts`: interfaccia `Persona` usa `nome`, ma il backend restituisce
  `firstName`/`lastName` → campi vuoti
- `auth-service.ts` (frontend): `logout()` non chiama `userState.clearUser()`
- `auth-guard.ts`: il risultato di `checkToken()` è ignorato → ritorna sempre `true`

---

## Verifiche già fatte (per non rifarle)

- **Docker Compose**: 6 servizi su rete `BARD`, solo `8080` esposta, PostgreSQL con
  volume persistente — verificato end-to-end
- **Flusso reale**: registrazione 201 → login → `/api/auth/me` 200 → `/api/ballerino` 200
- **Test Maven**: `BUILD SUCCESS` (7 moduli, tutti verdi)
- **Kafka**: rimosso (nessun producer/consumer nel codice)
