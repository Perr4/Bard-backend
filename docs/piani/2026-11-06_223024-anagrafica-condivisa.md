# Piano — Anagrafica condivisa (Soluzione A)

**Data**: 2026-11-06 22:30 UTC · **Aggiornato**: 2026-11-06 (decisioni D1/D5/D6/D7 confermate)
**Stato**: piano da approvare — nessuna modifica al codice in questa fase
**Decisione presa**: **Soluzione A — anagrafica condivisa**

---

## Goal

Ogni persona ha **una sola anagrafica**, posseduta da `auth-service` (`UserAccount`).
`Ballerino` è un **profilo dell'utente**: ha un suo id primario e un riferimento `userId`;
i suoi DTO espongono sia l'anagrafica (letta da auth-service) sia i dati di dominio.

---

## Decisioni confermate

| # | Decisione | Scelta |
|---|---|---|
| **D1** | `dataNascita` | **a** — va in `UserAccount`. Tutto ciò che è della persona resta in `UserAccount` |
| **D5** | `Ballerino.id` vs `UserAccount.id` | **a** — **separati**: `Ballerino.id` resta PK proprio, `userId` è una colonna `UNIQUE` |
| **D6** | Lettura anagrafica | **b** — `BallerinoDto` include l'anagrafica **più** i dati del ballerino. Il servizio parte dall'id del ballerino → cerca `userId` → legge l'anagrafica da auth-service |
| **D7** | `presenza-service.Presenza` | **a** — punta a **`Ballerino.id`** (`ballerino_id` resta invariato) |
| **D8** | Servizio ballerini incompleto | **differito**: si completa dopo questa modifica |

---

## Domande ancora aperte (bloccano fasi 2-6)

**D2 — `eta`: salvata o calcolata?**
(a) resta salvata in `Ballerino` · (b) si calcola da `dataNascita` (che ora è in `UserAccount`)

**D3 — Quando nasce il profilo ballerino?**
(a) mai automaticamente: `POST /api/ballerino` con `userId` · (b) alla registrazione · (c) alla registrazione, profilo vuoto

**D4 — Esiste un utente che NON è un ballerino?** (admin, insegnante, genitore)
(a) sì → profilo opzionale (1:0..1) · (b) no → profilo obbligatorio (1:1)

**D9 — Sicurezza dell'endpoint interno di auth-service (necessario per D6=b)**
(a) `permitAll` (chiamata solo dalla rete interna) · (b) richiede ruolo admin · (c) token interno condiviso

---

## Current context / assumptions

Verificato al 2026-11-06 (sola lettura).

**Repo**: 5 repo git separati, tutti su branch `develop`:
`auth-service`, `ballerini-service`, `presenza-service`, `gateway`, `eureka-service`.
`BARD/` **non** è un repo git. Frontend `BARD-Front` è un repo separato (escluso).

**Entità oggi**

| Servizio | File | Campi |
|---|---|---|
| auth | `src/main/java/it/chronos/auth/model/UserAccount.java` | `id`, `email`, `password`, `firstName`, `lastName`, `enabled`, `createdAt`, `updatedAt`, `roles` |
| ballerini | `src/main/java/io/bard/learning_service/model/Ballerino.java` | `id` (`ballerino_id`), `nome`, `cognome`, `eta`, `dataNascita`, `sezione`, `sesso`, `presenze` |
| presenza | `src/main/java/io/bard/presenza_service/model/Presenza.java` | `presenzaId`, `date`, `ballerino_id` |
| ballerini | `src/main/java/io/bard/learning_service/model/Presenza.java` | `presenzaId`, `date` (**duplicata**, differita) |

**Config rilevante**
- `ballerini-service/src/main/resources/application.yaml` → `ddl-auto: update` (Hibernate crea lo schema)
- `presenza-service/src/main/resources/application.yaml` → `ddl-auto: validate`
- `auth-service/src/main/resources/application.yml` → `ddl-auto: none`
- `sql/schema.sql` copre `presenza`, `auth_users`, `auth_roles`, `auth_user_roles`; **non** copre i ballerini
- `BallerinoService` incompleto (`modificaBallerino` → `null`, `getBallerino` vuoto) — **differito**

---

## Architecture / proposed approach

`auth-service` possiede identità + anagrafica. `ballerini-service` possiede il profilo di
dominio e, **alla lettura**, compone il DTO chiedendo l'anagrafica a `auth-service`
(tramite un endpoint interno). Il legame è `Ballerino.userId → UserAccount.id` (1:1 o 1:0..1
in base a D4). `presenza-service` non cambia (punta a `Ballerino.id`).

```
GET /api/ballerino/{id}
      │
      ▼
ballerini-service  ──(HTTP interno)──►  auth-service
      │                                       │
      ▼                                       ▼
 Ballerino + userId                    UserAccount (anagrafica)
      └────────────── composizione ───────────┘
                     BallerinoDto
```

---

## Step-by-step tasks

> Convenzione: `cd /media/myfiles/SSD/Code/BARD/bard-backend/<servizio>` +
> `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` + `mvn -B test`
> Atteso: `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` + `BUILD SUCCESS`.
> **TDD**: test rosso → implementazione minima → test verde → commit.
> Una sola esecuzione se verde; se fallisce, diagnosi e report.

### Fase 0 — Decisione
- [ ] **T0.1** Rispondere a **D2, D3, D4, D9**.
- [ ] **T0.2** Aggiornare `idee-strutturate.md` e `progetto-anagrafica-condivisa.md`.

### Fase 1 — auth-service: proprietario dell'anagrafica

**T1.1 — Aggiungere `dataNascita` a `UserAccount`** *(D1=a)*
File: `auth-service/src/main/java/it/chronos/auth/model/UserAccount.java`
La classe ha getter/setter scritti a mano (non Lombok): aggiungere il campo **e** i due metodi.
```java
    @Column(name = "data_nascita")
    private java.sql.Date dataNascita;
```
```java
    public java.sql.Date getDataNascita() { return dataNascita; }
    public void setDataNascita(java.sql.Date dataNascita) { this.dataNascita = dataNascita; }
```
Verifica: `mvn -B test` → BUILD SUCCESS.

**T1.2 — Esporre l'anagrafica per id (necessario per D6=b)**
File: `auth-service/src/main/java/it/chronos/auth/dto/AuthDtos.java`
Aggiungere un record di solo lettura:
```java
    /**
     * Anagrafica di un utente, letta dal servizio interno (ballerini-service).
     */
    public record UserAnagrafica(
            UUID userId,
            String firstName,
            String lastName,
            java.sql.Date dataNascita) {
    }
```
File: `auth-service/src/main/java/it/chronos/auth/controller/AuthController.java`
```java
    @GetMapping("/internal/users/{id}")
    public AuthDtos.UserAnagrafica getAnagrafica(@PathVariable UUID id) {
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new AuthDtos.UserAnagrafica(
                user.getId(), user.getFirstName(), user.getLastName(), user.getDataNascita());
    }
}
```
Sicurezza: impostare il matcher in
`auth-service/src/main/java/it/chronos/auth/config/SecurityConfig.java` secondo **D9**.

**T1.3 — Test dell'endpoint (TDD)**
File: `auth-service/src/test/java/it/chronos/auth/AuthControllerIntegrationTest.java`
- crea un utente, chiama `GET /api/auth/internal/users/{id}`, aspetta 200 con nome/cognome/data corretti.

### Fase 2 — ballerini-service: profilo con `userId`

**T2.1 — Modificare `Ballerino`**
File: `ballerini-service/src/main/java/io/bard/learning_service/model/Ballerino.java`
Rimuovere `nome`, `cognome`, `dataNascita`; aggiungere:
```java
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;
```
Tenere `sezione`, `eta` (se D2=a), `sesso`, `presenze`.

**T2.2 — Modificare `BallerinoDto`** *(D6=b)*
File: `ballerini-service/src/main/java/io/bard/learning_service/dto/BallerinoDto.java`
Rimuovere `nome`, `cognome`, `dataNascita`; aggiungere:
```java
    private UUID userId;
    // anagrafica letta da auth-service (sola lettura)
    private String firstName;
    private String lastName;
    private java.sql.Date dataNascita;
```

**T2.3 — Modificare `BallerinoCreate`**
File: `ballerini-service/src/main/java/io/bard/learning_service/dto/BallerinoCreate.java`
Rimuovere `nome`, `cognome`, `dataNascita`; aggiungere `@NotNull private UUID userId;`

**T2.4 — Validazione**
```java
    @NotNull(message = "l'id utente e obbligatorio")
    private UUID userId;
```
Verifica: `mvn -B test` → BUILD SUCCESS.

**T2.5 — Client HTTP verso auth-service**
File nuovo: `ballerini-service/src/main/java/io/bard/learning_service/config/AuthServiceClient.java`
(RestTemplate o WebClient, con URL da configurazione `auth.service.url`).

**T2.6 — Comporre il DTO in `BallerinoService`**
File: `ballerini-service/src/main/java/io/bard/learning_service/service/BallerinoService.java`
- in lettura: `Ballerino` → `userId` → chiamata al client → popola `firstName`/`lastName`/`dataNascita`
- **non** implementare ancora `modificaBallerino`/`getBallerino` (differito)

**T2.7 — Controller**
File: `ballerini-service/src/main/java/io/bard/learning_service/controller/BallerinoController.java`
- `@Valid` su `saveNewBallerino`, rimuovere `//AGGIUNGERE VALIDAZIONE` e `System.out.println`

**T2.8 — Test (TDD)**
File: `ballerini-service/src/test/java/io/bard/learning_service/BallerinoControllerIntegrationTest.java`
- create con `userId` valido → 200 con anagrafica composta
- create senza `userId` → 400
- create con `userId` inesistente → 404 (o 400, da decidere)

### Fase 3 — presenza-service
- **Nessuna modifica** (D7=a): `ballerino_id` resta e punta a `Ballerino.id`.
- Opzionale, separato: correggere l'import errato
  `jakarta.ws.rs.QueryParam` → `org.springframework.data.repository.query.Param` in
  `presenza-service/src/main/java/io/bard/presenza_service/repo/PresenzaRepo.java`.

### Fase 4 — Schema
- [ ] **T4.1** `sql/schema.sql`: aggiungere `data_nascita` a `auth_users`.
- [ ] **T4.2** `sql/schema.sql`: aggiungere la tabella `bard.ballerini`:
```sql
CREATE TABLE IF NOT EXISTS bard.ballerini (
    ballerino_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL UNIQUE,
    sezione       VARCHAR(32),
    eta           INTEGER,
    sesso         VARCHAR(16)
);
```
- [ ] **T4.3** Decidere `ddl-auto` di `ballerini-service` (`update` oggi): se si passa a
  `validate`, lo schema sopra diventa obbligatorio; altrimenti resta riferimento.

### Fase 5 — Differito
- Completamento di `BallerinoService` (`modificaBallerino`, `getBallerino`) — dopo questa modifica.
- Le due entità `Presenza` — dopo.

### Fase 6 — Verifica e commit
- [ ] **T6.1** `mvn -B test` su tutti e 5 i servizi (una run ciascuno).
- [ ] **T6.2** Commit unico per repo (frontend escluso).
- [ ] **T6.3** Push su `develop` (richiede credenziali Gitea — vedi rischi).

---

## Tests / validation

```bash
cd /media/myfiles/SSD/Code/BARD/bard-backend/<servizio>
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn -B test                       # tutti i test
mvn -B test -Dtest=NomeTest       # un solo test
```
Atteso: `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` + `BUILD SUCCESS`.

**Copertura finale richiesta**
- `Ballerino` non ha più `nome`/`cognome`/`dataNascita`
- `Ballerino.userId` obbligatorio e unico
- `BallerinoDto` espone anagrafica + dati ballerino
- un `userId` inesistente viene rifiutato
- i test di `presenza-service` restano verdi

**Test attuali (verdi, una run)**: presenza 6 · auth 9 · ballerini 1 · gateway 1 · eureka 1.

---

## Risks, tradeoffs, and open questions

**Rischi**
- **FK logica**: `Ballerino.userId` non è una FK fisica (DB distinti) → serve validazione applicativa; rischio profilo orfano se l'utente viene cancellato.
- **Dipendenza runtime**: per D6=b `ballerini-service` chiama `auth-service` a ogni lettura → latenza e accoppiamento; se auth è giù, la lettura dei ballerini fallisce.
- **`BallerinoService` incompleto**: va completato in un secondo momento (concordato).
- **`ddl-auto: update`** in ballerini: decidere se passare a `validate`.
- **Push**: Gitea `192.168.1.22:4000` senza credenziali in `~/.git-credentials`.

**Tradeoff**
- A si tiene la separazione dei servizi → meno rischio, ma si gestisce il legame tra due servizi.
- Una sola copia dell'anagrafica → niente duplicazione, ma la composizione del DTO costa una chiamata.

**Domande aperte**
- Chiudere **D2, D3, D4, D9**.
- Cosa si fa degli utenti esistenti senza profilo ballerino?
- Il frontend va adattato ai nuovi DTO? (fuori da questa repo)
- `Sezione` (`GRUPPO`/`Canto`/`SOSTENITORE`) appartiene al ballerino o all'utente?
