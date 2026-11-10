# To-do — BARD backend

Cose da fare, ordinate per priorita'. Le voci `[x]` sono fatte e verificate.

---

## A. Entita' di lettura "presenza con persona"  ← focus attuale

- [x] `GET /api/ballerino/{id}` — ballerino singolo con anagrafica (base creata)
- [ ] **Completare la dashboard**: includere le presenze nel DTO (il repository ha
      gia' `@EntityGraph(attributePaths = "presenze")`, ma nessun endpoint le espone)
- [ ] `GET /api/presenza` — tutte le presenze (oggi 405)
- [ ] `PresenzaConPersonaDto` — presenza + anagrafica
- [ ] Endpoint batch anagrafiche in auth-service (evita il N+1)
- [ ] Client HTTP + composizione in presenza-service
- [ ] Test del servizio
- [ ] (opzionale) eta' e sezione dal ballerini-service

Piano dettagliato: `piano-presenza-con-persona.md`

---

## F. Esportazione PDF

- [ ] Decidere: PDF generato dal **backend** (`GET .../export` → `application/pdf`)
      oppure dal **frontend** (libreria client)
- [ ] Definire il perimetro: solo le presenze di una persona, o anche tutte
- [ ] Layout del documento (intestazione, tabella, totale)

---

## B. Bug aperti

- [ ] `DELETE /api/presenza/{id}`: l'id e' della presenza, ma il frontend passa un
      `ballerino_id` → cancella la riga sbagliata
- [ ] `PUT /api/ballerino` non implementato (`modificaBallerino` ritorna `null`)
- [ ] `auth-service` con `.anyRequest().permitAll()`: protezione solo al gateway
      (da confermare se e' voluto)
- [ ] `user-by-id` (frontend): pagina raggiungibile ma vuota
- [ ] `uscite-page` (frontend): stub non registrato nelle route

---

## C. Test di integrazione

- [ ] Test che avvia lo stack e verifica login → token → chiamata autenticata
      (i bug di ieri non si vedevano dai test unitari)

### Verifica obbligatoria per ogni endpoint (fatta da Perra)
Ogni endpoint nuovo o modificato va testato con TRE chiamate:
- [ ] senza token → **401**
- [ ] token falso → **401**
- [ ] token valido → **200**

---

## D. Documentazione

- [ ] Aggiornare `README.md` e `docs/README.md` con i nuovi endpoint
- [ ] `docs/ROADMAP.md`: le voci della Fase 1 sono completate, manca l'aggiornamento

---

## E. Backend: cosa dovra' saper fare

| Capacita' | Stato |
|---|---|
| Autenticazione (register, login, JWT) | ✅ |
| Verifica token (`/api/auth/token`) | ✅ |
| Profilo utente (`/api/auth/me`) | ✅ |
| CRUD ballerini + anagrafica composita | ✅ |
| CRUD presenze | ✅ |
| Lista presenze per ballerino | ✅ |
| Lista di tutte le presenze | ❌ |
| Presenza con dati della persona | ❌ |
| Composizione batch (niente N+1) | ❌ |
