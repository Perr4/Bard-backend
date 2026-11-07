# Progetto — Anagrafica condivisa (utenti e ballerini)

**Stato**: idea accettata — da ampliare, non ancora da attuare.
**Decisione**: **Soluzione A — anagrafica condivisa**.
**Data**: 06/11/2026

> Questo documento contiene l'idea. Non è un piano di attuazione: i dettagli
> (modello dati, API, migrazione) si definiranno quando l'idea verrà ampliata.

---

## 1. L'idea in breve

Ogni persona ha **una sola anagrafica**, posseduta da `auth-service`.
Il ballerino **non è una seconda persona**: è un **profilo dell'utente**.

Oggi `UserAccount` (auth-service) e `Ballerino` (ballerini-service) rappresentano la
stessa persona con due entità diverse, e l'anagrafica (nome, cognome, data di nascita)
rischia di esistere in due posti. L'idea è averne una sola copia.

---

## 2. Il modello concettuale

```
        ┌─────────────────────────┐
        │      auth-service       │
        │  ┌───────────────────┐  │
        │  │    UserAccount    │  │   ← identità + anagrafica
        │  │  email, password  │  │      (nome, cognome, dataNascita)
        │  │  firstName        │  │
        │  │  lastName         │  │
        │  │  roles            │  │
        │  └─────────┬─────────┘  │
        └────────────┼────────────┘
                     │ userId
                     ▼
        ┌─────────────────────────┐
        │    ballerini-service    │
        │  ┌───────────────────┐  │
        │  │     Ballerino     │  │   ← profilo di dominio
        │  │  userId           │  │      (sezione, età, sesso)
        │  │  sezione          │  │
        │  │  eta, sesso       │  │
        │  │  presenze         │  │
        │  └───────────────────┘  │
        └─────────────────────────┘
```

- **`auth-service` possiede l'identità**: email, credenziali, ruoli, e **l'anagrafica**.
- **`ballerini-service` possiede il profilo**: solo i dati che riguardano il ballerino.
- **Il legame è `Ballerino.userId → UserAccount.id`**.

---

## 3. Cosa significa in pratica (a livello di idea)

- **Un solo nome per persona**: niente più `firstName`/`lastName` in un servizio e
  `nome`/`cognome` nell'altro.
- **Il ballerino esiste solo se l'utente esiste**: non puoi avere un profilo ballerino
  "orfano".
- **Utenti senza profilo ballerino sono ammessi**: un utente può esistere con un ruolo
  (insegnante, admin) senza essere un ballerino. Questo è possibile proprio perché
  il profilo è opzionale.
- **I servizi restano separati**: non si fonde nulla; resta il confine netto tra
  "chi sei" (auth) e "cosa fai" (ballerini).

---

## 4. Perché la soluzione A

- **Meno punti di modifica**: non si tocca la struttura dei servizi, non si sposta
  un'intera anagrafica da un servizio all'altro.
- **Nessuna fusione**: `auth-service` e `ballerini-service` restano due servizi.
- **Nessuna sincronizzazione**: non serve un meccanismo per allineare due copie dei dati,
  perché ne esiste una sola.
- **Semplicità**: la programmazione e la comprensione restano lineari.

---

## 5. Cosa **non** si fa in questa fase

- No schema SQL definitivo
- No codice, nessuna modifica a entità o API
- No migrazione
- Nessuna decisione su come creare il profilo ballerino (alla registrazione o dopo)

---

## 6. Cosa resta aperto (da definire nell'ampliamento)

- [ ] `Ballerino.id` e `UserAccount.id` coincidono o sono separati?
- [ ] Che campi ha l'anagrafica oltre a nome e cognome?
- [ ] Quando nasce il profilo ballerino: alla registrazione o in un secondo momento?
- [ ] Come si presenta l'anagrafica nelle API del ballerino (due chiamate o una vista)?
- [ ] Il legame è 1:1 obbligatorio o 1:0..1?
- [ ] Cosa si fa delle due entità `Presenza` (una per servizio, campi diversi)?

---

## 7. Come si evolve

1. **Idea** — *questo documento*
2. **Ampliamento** — definire il modello, le API, la migrazione, il legame
3. **Attuazione** — TDD, un servizio alla volta, verifica e commit

---

## 8. Riferimenti

- Diagnosi del problema: `idee-strutturate.md` (voce 1)
- Note originali: `idee.md`
- Piano precedente (da rivedere alla luce di questa decisione):
  `roadmap-unificazione-ballerini-utenti.md`
