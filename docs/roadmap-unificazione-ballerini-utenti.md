# Piano — Unificare ballerini-service e utenti

> Idea: **ogni utente è un ballerino**.
> File di lavoro per l'implementazione, da aggiornare insieme.

Stato: **piano** — non ancora implementato.

---

## 1. Obiettivo

Oggi i due concetti vivono separati:

- `auth-service` → `auth_users` (credenziali, ruoli, JWT)
- `ballerini-service` → anagrafica ballerini (al momento **non implementata**: il servizio è stato ridotto a config + exception handler)

L'obiettivo è far sì che **l'utente della piattaforma sia un ballerino**, senza duplicare
la stessa persona in due luoghi.

---

## 2. Modello dati scelto

**Opzione A — `Ballerino` come profilo dell'utente** (scelta di partenza)

| Entità | Servizio | Contenuto |
|---|---|---|
| `UserAccount` | auth-service | credenziali, ruoli, JWT |
| `Ballerino` | ballerini-service | anagrafica: nome, cognome, contatti, sezione, ... |

Legame: `Ballerino.userId` → `UserAccount.id` (UUID), relazione 1:1.

**Perché A**: le credenziali restano in auth (sicurezza, JWT, ruoli), l'anagrafica resta nel
servizio dei ballerini (dominio). Ogni utente ha un profilo ballerino; il ballerino non esiste
senza utente.

**Domande da chiarire prima di partire**
- [ ] Un utente può esistere **senza** profilo ballerino? (es. amministratore, insegnante)
- [ ] Il legame è 1:1 obbligatorio o 1:0..1?
- [ ] Chi crea il profilo ballerino: alla registrazione, o in un secondo momento?
- [ ] Che campi ha l'anagrafica? (nome, cognome, data di nascita, telefono, sezione, note)
- [ ] Il nome/cognome restano in `auth_users` o migrano in `Ballerino`?

---

## 3. Impatto sui servizi

### auth-service
- [ ] `UserAccount`: valutare se tenere `firstName`/`lastName` o delegarli a `Ballerino`
- [ ] `register`: creare l'utente **e** il profilo ballerino collegato (o solo l'utente, a seconda della risposta sopra)
- [ ] `GET /api/auth/me`: restituire anche i dati del profilo ballerino?
- [ ] Evento Kafka `chronos.user.registered`: ora commentato — valuta se riattivarlo per far creare il profilo a ballerini-service

### ballerini-service
- [ ] Entità `Ballerino` con `userId` (FK logica verso `auth_users`)
- [ ] Repository, service, controller (CRUD)
- [ ] DTO + validazione Bean Validation
- [ ] Test: context load (già presente), repository, controller
- [ ] Decidere se il servizio espone dati di un singolo ballerino o solo liste/ricerche

### presenza-service
- [ ] `Presenza.ballerino_id` → rinominare in `userId`? O mantenere `ballerino_id` come chiave logica?
- [ ] Se il modello cambia, aggiornare DTO, service, repo e test

### gateway
- [ ] Rotte per il nuovo endpoint dei ballerini (`/api/ballerino/**` esiste già)
- [ ] Autorizzazione: chi può leggere/modificare un profilo ballerino?

### Schema
- [ ] `sql/schema.sql`: aggiungere `bard.ballerini` con `user_id` UNIQUE
- [ ] `sql/schema-h2.sql`: aggiornare lo spunto H2 di conseguenza

---

## 4. Fasi di implementazione (TDD)

### Fase 0 — Decisione
- [ ] Rispondere alle domande della sezione 2
- [ ] Scegliere 1:1 obbligatorio vs 1:0..1
- [ ] Scegliere chi crea il profilo e quando

### Fase 1 — Schema e entità
- [ ] Aggiungere `bard.ballerini` a `sql/schema.sql`
- [ ] Creare l'entità `Ballerino` con `userId`
- [ ] Test: verificare il mapping JPA (context load + `@DataJpaTest`)

### Fase 2 — Repository
- [ ] `BallerinoRepo` con `findByUserId`
- [ ] Test di repository: salvataggio, ricerca per userId, unicità

### Fase 3 — Service e controller
- [ ] `BallerinoService`: create, update, get by userId
- [ ] `BallerinoController`: endpoint CRUD
- [ ] DTO + validazione
- [ ] Test del controller (MockMvc)

### Fase 4 — Integrazione con auth
- [ ] `register` crea utente + profilo (o solo utente)
- [ ] `GET /api/auth/me` arricchito con i dati del profilo
- [ ] Test di integrazione: registrazione completa

### Fase 5 — Allineamento presenza
- [ ] Decidere il nome del campo (`ballerino_id` vs `userId`)
- [ ] Aggiornare entità, DTO, service, repo, test

### Fase 6 — Verifica e commit
- [ ] Eseguire i test di tutti i servizi
- [ ] Commit unico (come da convenzione)
- [ ] Push su `develop`

---

## 5. Rischi e note

- **Due DB logici**: auth e ballerini sono servizi separati; il legame `userId` è una FK
  logica, non fisica. Serve coerenza applicativa (evento Kafka o chiamata sincrona).
- **Evento Kafka**: il publisher è attualmente commentato in auth-service. Se si usa la via
  asincrona, bisogna riattivarlo e creare il consumer in ballerini-service.
- **Migrazione**: se esistono già utenti senza profilo, serve una strategia (profilo vuoto
  automatico o flag "completato").
- **Frontend**: `BARD-Front` ha già pagine User; il modello unificato potrebbe semplificarle.

---

## 6. Prossimo passo immediato

Rispondere alle 5 domande della sezione 2. Senza quelle risposte il modello dati non è stabile
e ogni fase dopo rischia di essere rifatta.
