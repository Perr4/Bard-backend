# Idee

> File personale: Perra inserisce qui le sue idee, in qualsiasi forma.
> Hermes le riorganizza su richiesta.
> Le idee riorganizzate sono in `idee-strutturate.md`.

## Idee registrate

### Duplicazione identità: UserAccount (auth-service) e Ballerino (ballerini-service)

Il problema: `Ballerino` e l'entità utente dell'`auth-service` sono due identità molto
simili — gli utenti dell'`auth-service` svolgono, in pratica, lo stesso ruolo dei ballerini.

- Stato: **bozza**, da decidere
- Duplicazione reale: **solo l'anagrafica** (`firstName`/`lastName` ↔ `nome`/`cognome`, `dataNascita`)
- Non duplicato: credenziali e ruoli (solo auth), sezione/età/sesso/presenze (solo ballerino)
- Reading chiave: non sono "lo stesso ruolo", sono **la stessa persona con due responsabilità diverse**
- Opzioni: **A. anagrafica condivisa** (auth possiede l'identità, Ballerino referencia `userId`) —
  raccomandata; **B. fusione completa** in un servizio; **C. due entità separate con duplicazione**
- Domanda decisiva: **esiste un utente che non è un ballerino?** (admin, insegnante, genitore)
  → se no, la fusione è più semplice; se sì, l'opzione A è obbligatoria
- Prossimo passo: rispondere la domanda decisiva e scegliere il modello
