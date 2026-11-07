# Piano — DTO compositi + risoluzione N+1

**Data**: 2026-11-06 · **Stato**: piano da approvare — nessuna modifica eseguita
**Richiesta**: «rendi i DTO, per il momento un DTO deve usare un altro DTO, quindi solo dati non sensibili e poi corregge il problema della N+1»

---

## Goal

1. **DTO compositi**: un DTO di uscita contiene un **altro DTO** (non campi anagrafici sparsi), con **solo dati non sensibili**.
2. **N+1**: `getAllBallerini()` fa oggi una chiamata HTTP a auth-service per ogni ballerino → servirà una lettura **batch**.

---

## Current context

Verificato al 2026-11-06 (sola lettura).

**Stato attuale dei DTO**

| File | Contenuto |
|---|---|
| `auth-service/src/main/java/it/chronos/auth/dto/AuthDtos.java` | record `UserAnagrafica(UUID userId, String firstName, String lastName, java.sql.Date dataNascita)` |
| `ballerini-service/src/main/java/io/bard/learning_service/dto/BallerinoDto.java` | `id`, `userId`, `firstName`, `lastName`, `dataNascita`, `eta`, `sezione` — **campi sparsi** |
| `ballerini-service/src/main/java/io/bard/learning_service/dto/BallerinoCreate.java` | `userId` (`@NotNull`), `eta`, `sezione`, `sesso` |
| `ballerini-service/src/main/java/io/bard/learning_service/config/AuthServiceClient.java` | client HTTP + **duplicato** record `UserAnagrafica` |

**Problemi attuali**
1. `BallerinoDto` ha i campi anagrafici **spalmati** (`firstName`, `lastName`, `dataNascita`) invece di un oggetto anagrafica.
2. Esistono **due** record `UserAnagrafica`: uno in auth-service (`AuthDtos`) e uno duplicato in `AuthServiceClient`. Violano DRY.
3. `getAllBallerini()` → `toDto()` → **una chiamata HTTP per ballerino** (N+1).
4. `sesso` non è esposto in `BallerinoDto` (gap preesistente).
5. `deleteBallerino` legge l'anagrafica **dopo** aver cancellato il profilo.

---

## Decisioni

| # | Decisione | Scelta |
|---|---|---|
| **D10** | Come annidare il DTO | Il DTO del ballerino **annida l'oggetto utente** (un DTO utente con l'anagrafica), non campi sparsi |
| **D11** | Cosa esporre | L'**entità** `UserAccount` ha **tutti** i dati; il **DTO** espone `firstName`, `lastName`, `dataNascita` e **niente dati sensibili** (email, password, ecc.) |
| **D12** | N+1 | *(da confermare)* |
| **D13** | `sesso` in `BallerinoDto` | *(da confermare)* |
| **D14** | `deleteBallerino` | *(da confermare)* |

**Conseguenza di D10+D11**: il DTO annidato è un **DTO utente** (es. `UserAnagrafica`) che
contiene `firstName`, `lastName`, `dataNascita`. L'email **non** viaggia nel DTO del ballerino.

**Domande rimaste aperte**

**D12 — Come risolvere la N+1?**
- (a) **Endpoint batch** in auth-service: `POST /api/auth/internal/users/anagrafica` con lista di id → mappa di anagrafiche. Una sola chiamata per `getAllBallerini`.
- (b) **Cache** delle anagrafiche in ballerini-service (TTL breve) — niente chiamata extra alla prima, ma complessità di cache.
- (c) **Join lato DB** — impossibile: sono due DB distinti.

**D13 — `sesso` in `BallerinoDto`?**
- (a) Sì, aggiungerlo · (b) No, lasciarlo fuori

**D14 — `deleteBallerino`: quando leggere l'anagrafica?**
- (a) Leggere l'anagrafica **prima** di cancellare, poi cancellare (ora legge dopo)
- (b) Restituire il DTO senza anagrafica in delete

---

## Proposed approach

`BallerinoDto` conterrà un **oggetto anagrafica annidato** (D10) con solo dati non sensibili (D11).
Il service, per la lista, chiamerà auth-service **una sola volta** con tutti gli id (D12=a) e
compoterà i DTO con una mappa. Il record duplicato in `AuthServiceClient` viene rimosso.

---

## Step-by-step tasks

> Convenzione: `cd /media/myfiles/SSD/Code/BARD/bard-backend/<modulo>` +
> `JAVA_HOME=<jdk del pom>` + `mvn -B test`
> Atteso: `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` + `BUILD SUCCESS`.
> **TDD**: test rosso → implementazione minima → test verde → commit.
> Una run se verde; se fallisce, diagnosi e report.

### Fase 0 — Decisione
- [ ] **T0.1** Rispondere a **D10, D11, D12, D13, D14**.

### Fase 1 — DTO anagrafico condiviso

**T1.1 — Definire il DTO anagrafico** *(D10)*
Se D10=a: usare `it.chronos.auth.dto.AuthDtos.UserAnagrafica`.
Se D10=b: creare `ballerini-service/src/main/java/io/bard/learning_service/dto/AnagraficaDto.java`
con i campi scelti in D11.

**T1.2 — Rimuovere il duplicato**
File: `ballerini-service/src/main/java/io/bard/learning_service/config/AuthServiceClient.java`
Eliminare il record `UserAnagrafica` locale; usare quello condiviso (o `AnagraficaDto`).

**T1.3 — Test del DTO** *(TDD)*
Verificare che il DTO annidato serializzi correttamente (test di serializzazione o del controller).

### Fase 2 — `BallerinoDto` composito

**T2.1 — Modificare `BallerinoDto`**
File: `ballerini-service/src/main/java/io/bard/learning_service/dto/BallerinoDto.java`
Rimuovere `firstName`, `lastName`, `dataNascita`; annidare il DTO utente (D10):
```java
    private UUID id;
    private UUID userId;
    private AuthDtos.UserAnagrafica utente;   // annidato: firstName, lastName, dataNascita
    private Integer eta;
    private Sezione sezione;
    private String sesso;                     // se D13=a
```
Nessun campo sensibile (email, password) nel DTO.

**T2.2 — Test del DTO composito** *(TDD)*
File: `ballerini-service/src/test/java/io/bard/learning_service/BallerinoControllerIntegrationTest.java`
- la risposta contiene un oggetto `anagrafica` annidato con i valori corretti
- nessun campo sensibile (email, password) esposto

### Fase 3 — Risolvere la N+1

**T3.1 — Endpoint batch in auth-service** *(D12=a)*
File: `auth-service/src/main/java/it/chronos/auth/controller/AuthController.java`
```java
    @PostMapping("/internal/users/anagrafica")
    public List<AuthDtos.UserAnagrafica> getAnagrafiche(@RequestBody List<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .map(u -> new AuthDtos.UserAnagrafica(
                        u.getId(), u.getFirstName(), u.getLastName(), u.getDataNascita()))
                .toList();
    }
```
File: `auth-service/src/main/java/it/chronos/auth/config/SecurityConfig.java`
Estendere il matcher a `/api/auth/internal/**` (già fatto in D9=a).

**T3.2 — Test dell'endpoint batch** *(TDD)*
File: `auth-service/src/test/java/it/chronos/auth/AuthControllerIntegrationTest.java`
- 2 utenti → `POST` con 2 id → 200 con 2 anagrafiche
- id inesistente → non presente in risposta (o 404, da decidere)

**T3.3 — Client batch in ballerini-service**
File: `ballerini-service/src/main/java/io/bard/learning_service/config/AuthServiceClient.java`
Aggiungere:
```java
    public List<UserAnagrafica> findAnagrafiche(List<UUID> ids) { ... }
```

**T3.4 — `getAllBallerini` con una sola chiamata**
File: `ballerini-service/src/main/java/io/bard/learning_service/service/BallerinoService.java`
- raccogliere tutti gli `userId` dei ballerini
- **una** chiamata batch → mappa `userId → anagrafica`
- comporre i DTO dalla mappa

**T3.5 — Test N+1 risolto** *(TDD)*
Verificare che `getAllBallerini` con N ballerini faccia **1** chiamata HTTP (non N).
Tecnica: `MockRestServiceServer` o un contatore nel client.

### Fase 4 — Allineamenti minori

**T4.1 — `deleteBallerino`** *(D14)*
File: `ballerini-service/src/main/java/io/bard/learning_service/service/BallerinoService.java`
Leggere l'anagrafica **prima** di cancellare (o restituire il DTO senza anagrafica, se D14=b).

**T4.2 — `auth.service.url`**
File: `ballerini-service/src/main/resources/application.yaml`
Aggiungere `auth.service.url` (da configurare in deployment).

### Fase 5 — Verifica e commit
- [ ] **T5.1** `mvn -B test` su auth-service e ballerini-service (una run ciascuno).
- [ ] **T5.2** Commit unico per repo (frontend escluso).
- [ ] **T5.3** Push su `develop` (richiede credenziali Gitea).

---

## Tests / validation

```bash
cd /media/myfiles/SSD/Code/BARD/bard-backend/<modulo>
export JAVA_HOME=<jdk del pom>   # auth: 21, ballerini: 25
mvn -B test
```
Atteso: `Tests run: N, Failures: 0, Errors: 0, Skipped: 0` + `BUILD SUCCESS`.

**Copertura richiesta**
- `BallerinoDto` contiene un oggetto anagrafica annidato (non campi sparsi)
- nessun dato sensibile (email, password) nel DTO
- `getAllBallerini` fa **1** chiamata HTTP per N ballerini
- endpoint batch: 2 id → 2 anagrafiche
- `deleteBallerino` non fallisce se l'utente è assente (se D14=a)

**Test attuali (verdi)**: auth 11 · ballerini 4.

---

## Risks, tradeoffs, and open questions

**Rischi**
- **D10=a accoppia i servizi**: ballerini-service dipenderebbe dai DTO di auth-service. Se i
  servizi devono restare disaccoppiati, D10=b è più sicuro (ma duplica la forma).
- **Endpoint batch**: va protetto come l'altro endpoint interno (`/api/auth/internal/**`).
- **`MockRestServiceServer`** per il test N+1 richiede `spring-test` (già presente).

**Tradeoff**
- Il DTO composito è più leggibile per il frontend (un oggetto annidato) ma cambia la forma
  della risposta → **il frontend va adattato** (fuori da questa repo).

**Domande aperte**
- Chiudere **D10, D11, D12, D13, D14**.
- Il frontend si aspetta i campi piatti o annidati? (va verificato prima di cambiare la forma)
- `email` è dato sensibile o meno? (D11)
