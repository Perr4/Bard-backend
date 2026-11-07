# Idee strutturate

> Idee di progetto, riorganizzate dalle note grezze.
> Fonte: `idee.md` (note originali di Perra).

---

## 1. Duplicazione identità: `UserAccount` (auth-service) e `Ballerino` (ballerini-service)

**Idea**: risolvere la duplicazione tra l'entità utente dell'`auth-service` e l'entità
ballerino del `ballerini-service`: sono due entità molto simili, perché rappresentano la
stessa persona.

**Stato**: bozza — da decidere

### Contesto

| | `UserAccount` (auth-service) | `Ballerino` (ballerini-service) |
|---|---|---|
| Campi | `email`, `password`, `firstName`, `lastName`, `enabled`, `createdAt`, `updatedAt`, `roles` | `id` (`ballerino_id`), `nome`, `cognome`, `eta`, `dataNascita`, `sezione`, `sesso`, `presenze` |
| Responsabilità | **accesso**: chi sei, cosa puoi fare | **dominio**: cosa fai nella scuola |
| Ciclo di vita | registrazione, login, permessi | iscrizione, sezione, presenze |

### Diagnosi

- **Duplicazione reale**: solo l'**anagrafica** — `firstName`/`lastName` ↔ `nome`/`cognome`,
  e `dataNascita`. È l'unica parte che si sovrappone.
- **Non duplicato**: credenziali e ruoli (solo auth) e sezione/età/sesso/presenze (solo ballerino).
- **Errore diinterpretazione da evitare**: non è che "gli utenti svolgono lo stesso ruolo dei
  ballerini". È che sono **la stessa persona con due responsabilità diverse**. Trattarli come
  la stessa entità perderebbe:
  - la possibilità di avere utenti che non sono ballerini (admin, insegnante, genitore)
  - la separazione tra sicurezza e dominio (auth cresce e si accoppia alla didattica)

### Modello dati — opzioni

| Opzione | Descrizione | Pro | Contro |
|---|---|---|---|
| **A. Anagrafica condivisa** *(raccomandata)* | `auth-service` possiede l'identità e l'anagrafica (`UserAccount`); `Ballerino` referencia `userId` e contiene solo i campi di dominio (sezione, età, sesso, presenze) | una sola fonte di verità per il nome; responsabilità separate; supporta utenti non ballerini | due servizi, due DB logici; FK logica |
| B. Fusione completa | Un solo servizio con un'unica entità persona (credenziali + anagrafica + dominio) | massima semplicità, nessuna duplicazione, nessuna sincronizzazione | accoppia sicurezza e didattica; nessun utente "non ballerino" |
| C. Due entità separate con duplicazione | Mantenere `firstName`/`lastName` in entrambe | nessuna modifica | duplicazione reale dei dati, rischio di incoerenze |

### Domande aperte

- [ ] **Esiste un utente che non è un ballerino?** (admin, insegnante, genitore)
      → se **no**, la fusione (B) è più semplice; se **sì**, l'opzione A è obbligatoria.
- [ ] Il legame è 1:1 obbligatorio o 1:0..1?
- [ ] Chi crea il profilo ballerino: alla registrazione o in un secondo momento?
- [ ] Che campi ha l'anagrafica del ballerino oltre a nome e cognome?
- [ ] `Ballerino.id` e `UserAccount.id` coincidono o sono separati?

### Impatto

- `auth-service`: `UserAccount` diventa l'unica fonte dell'anagrafica
- `ballerini-service`: `Ballerino` perde `nome`/`cognome`/`dataNascita`, guadagna `userId`
- `presenza-service`: `Presenza.ballerino_id` → rinominare in `userId`?
- Schema: `auth_users` e `ballerini`
- API: registrazione (utente + profilo), profilo ballerino

### Prossimi passi

1. Rispondere alle domande aperte (in particolare la prima)
2. Scegliere il modello (A, B o C)
3. Definire il legame e la migrazione
4. Pianificare le modifiche per servizio (TDD)

---

## 2. Due tipi di presenza

**Idea**: introdurre **due tipi di presenza** nel servizio delle presenze.

**Stato**: idea futura — ampliamento previsto

**Contesto**
- `presenza-service` ha `Presenza` con `presenzaId`, `date`, `ballerino_id`
- `ballerini-service` ha una sua `Presenza` con solo `presenzaId`, `date` — **attenzione**:
  sono due entità con lo stesso nome in due servizi diversi, con campi diversi

**Domande aperte**
- Come modellare il tipo: campo `tipo` (enum/stringa) sulla stessa tabella, oppure due entità separate?
- Le due tipologie hanno campi diversi o solo un significato diverso?
- Due presenze duplicate (una per servizio) sono volute o sono un'altra duplicazione da risolvere?

**Prossimi passi**
1. Chiarire il rapporto tra le due entità `Presenza`
2. Definire le due tipologie e il loro significato
3. Scegliere la modellazione (campo vs entità)
4. Valutare la migrazione dello schema

---

## Note operative

- Le idee restano in questo file in forma strutturata; le note grezze restano in `idee.md`.
- Quando un'idea passa a lavoro, spostarla in `ROADMAP.md` con stato e priorità.
