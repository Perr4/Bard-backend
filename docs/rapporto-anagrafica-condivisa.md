# Rapporto — Anagrafica condivisa (Soluzione A)

**Data**: 2026-11-06
**Stato**: implementazione completata e verificata, **non pushata**
**Commit**: `auth-service` `8428e33` · `ballerini-service` `c4613fb`

---

## 1. Riassunto

È stata attuata la **Soluzione A — anagrafica condivisa**: l'anagrafica della persona
(nome, cognome, data di nascita) vive in `auth-service` (`UserAccount`); il ballerino è un
**profilo** che punta all'utente tramite `userId` e ne espone i dati anagrafici componendo
il DTO.

Lavoro fatto in due passi orchestrati:
1. **agente di implementazione** — ha scritto codice e test, un commit per repo
2. **agente di verifica indipendente** — ha riconfermato commit, codice e test senza toccare nulla

---

## 2. Decisioni applicate

| # | Decisione | Scelta |
|---|---|---|
| D1 | `dataNascita` | **a** — in `UserAccount` (anagrafica) |
| D2 | `eta` | **a** — resta salvata in `Ballerino` |
| D3 | Creazione profilo | **a** — `POST /api/ballerino` + `userId`, niente auto-creazione |
| D4 | Utente non-ballerino | **a** — profilo opzionale (1:0..1) |
| D5 | Id | **a** — `Ballerino.id` resta PK, `userId` è `UNIQUE NOT NULL` |
| D6 | Lettura anagrafica | **b** — `BallerinoDto` include anagrafica + dati ballerino, composta via chiamata a auth-service |
| D7 | `presenza-service` | **a** — punta a `Ballerino.id`, **nessuna modifica** |
| D8 | `BallerinoService` incompleto | **differito** |
| D9 | Endpoint interno | **a** — `permitAll` su `/api/auth/internal/**` |

---

## 3. Cosa è stato fatto

### 3.1 auth-service — commit `8428e33`

Messaggio: *"feat: espone l'anagrafica dell'utente via endpoint interno"*

File modificati (5):

| File | Modifica |
|---|---|
| `src/main/java/it/chronos/auth/model/UserAccount.java` | + campo `dataNascita` (`java.sql.Date`, colonna `data_nascita`) con getter/setter scritti a mano |
| `src/main/java/it/chronos/auth/dto/AuthDtos.java` | + record `UserAnagrafica(UUID userId, String firstName, String lastName, java.sql.Date dataNascita)` |
| `src/main/java/it/chronos/auth/controller/AuthController.java` | + endpoint `GET /api/auth/internal/users/{id}` → 200 con anagrafica, 404 se assente |
| `src/main/java/it/chronos/auth/config/SecurityConfig.java` | + matcher `/api/auth/internal/**` → `permitAll` (D9=a) |
| `src/test/java/it/chronos/auth/AuthControllerIntegrationTest.java` | **nuovo** — test TDD dell'endpoint |

Test: `Tests run: 11, Failures: 0, Errors: 0, Skipped: 0` → **BUILD SUCCESS** (JDK 21)
Composizione: `AuthDtosTest` 8 · `AuthServiceApplicationTests` 1 · `AuthControllerIntegrationTest` 2

### 3.2 ballerini-service — commit `c4613fb`

Messaggio: *"feat: profilo ballerino legato all'utente con anagrafica condivisa"*

File modificati/creati (10):

| File | Modifica |
|---|---|
| `src/main/java/io/bard/learning_service/model/Ballerino.java` | − `nome`, `cognome`, `dataNascita`; + `@Column(name="user_id", nullable=false, unique=true) UUID userId`; restano `eta`, `sezione`, `sesso`, `presenze` |
| `src/main/java/io/bard/learning_service/dto/BallerinoDto.java` | − anagrafica; + `userId`, `firstName`, `lastName`, `dataNascita` (lettura) |
| `src/main/java/io/bard/learning_service/dto/BallerinoCreate.java` | − anagrafica; + `@NotNull UUID userId` |
| `src/main/java/io/bard/learning_service/config/AuthServiceClient.java` | **nuovo** — client verso auth-service (`auth.service.url`, default `http://localhost:7999`) |
| `src/main/java/io/bard/learning_service/config/GeneralConfig.java` | + bean `RestTemplate`; fix mapping ModelMapper |
| `src/main/java/io/bard/learning_service/service/BallerinoService.java` | composizione del DTO: `Ballerino` → `userId` → anagrafica; 400 se `userId` inesistente |
| `src/main/java/io/bard/learning_service/controller/BallerinoController.java` | + `@Valid`; − `//AGGIUNGERE VALIDAZIONE`; − `System.out.println` |
| `pom.xml` | + `spring-boot-starter-validation`, + H2 (test), + `maven-compiler-plugin` `<proc>full</proc>` |
| `src/test/java/io/bard/learning_service/BallerinoControllerIntegrationTest.java` | **nuovo** — 3 test TDD |
| `src/test/resources/application.yml` | **nuovo** — H2 in memoria per i test (isolamento dal DB di sviluppo) |

Test: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0` → **BUILD SUCCESS** (JDK 25)
Composizione: `BalleriniServiceApplicationTests` 1 · `BallerinoControllerIntegrationTest` 3

### 3.3 Schema (file condiviso, fuori repo git)

`bard-backend/sql/schema.sql`:
- + colonna `data_nascita DATE` su `bard.auth_users`
- + tabella `bard.ballerini` (`ballerino_id` PK, `user_id UUID NOT NULL UNIQUE`, `sezione`, `eta`, `sesso`)

Nota: `ballerini-service` usa `ddl-auto: update`, quindi Hibernate crea la tabella a runtime;
`schema.sql` resta il riferimento per l'eventuale passaggio a `validate`.

### 3.4 Verifica indipendente

Un secondo agente, senza accesso in scrittura, ha verificato tutto: **20/20 PASS**,
nessuna discrepanza tra il resoconto di implementazione e la realtà.
- commit e alberi confermati, nessun file non committato
- codice conforme alle decisioni D1/D2/D3/D5/D6/D7/D9
- `presenza-service` (`2734cf5`), `gateway` (`2bcc5c3`), `eureka-service` (`cf7f8d2`) **invariati**
- `BARD-Front` intoccato (`442d0bc`)
- test ri-eseguiti: 11 + 4 verdi

---

## 4. Problemi emersi durante l'implementazione

1. **Lombok non generava i getter** con JDK 23+ (annotation processing implicito disabilitato).
   Fix: `maven-compiler-plugin` con `<proc>full</proc>`.
2. **`ballerini-service` richiede Java 25** (`<java.version>25</java.version>`): i test sono stati
   eseguiti con `/usr/lib/jvm/java-25-openjdk-amd64`, non con Java 21.
3. **`spring-boot-starter-validation` mancava**: in Boot 4 `spring-boot-starter-webmvc` non lo
   include più; senza di esso `@NotNull` non compila.
4. **Ambiguità ModelMapper**: `BallerinoCreate.userId` veniva mappato anche su `Ballerino.id`
   (matching STANDARD: `id` ⊂ `userId`) → `merge()` → `StaleObjectStateException`.
   Fix: mapping esplicito che salta `Ballerino::setId`.
5. **`RestTemplateBuilder` non esiste in Boot 4.1** → bean `RestTemplate` dichiarato direttamente.
6. **I test ballerini ora girano su H2** in memoria, per non toccare il DB di sviluppo condiviso
   (`bard.ballerino` ha 2 righe esistenti, che un `user_id NOT NULL UNIQUE` avrebbero reso incompatibili).

---

## 5. Cosa penso sia da fare dopo

### Priorità 1 — Bloccanti per l'avvio

**1.1 Migrazione del DB di sviluppo**
Lo schema a runtime non è allineato:
- `auth_users` non ha `data_nascita` (auth usa `ddl-auto: none`)
- `ballerino` non ha `user_id` e conserva ancora `nome`/`cognome`/`data_nascita`

Senza migrazione l'app non si avvia correttamente. Serve:
- aggiungere la colonna `data_nascita` a `auth_users`
- aggiungere `user_id` a `ballerino` (colonna nullable + backfill delle 2 righe esistenti + poi `NOT NULL UNIQUE`)
- valutare se rimuovere le colonne anagrafiche da `ballerino`

**1.2 Registrare `dataNascita` in fase di registrazione**
`RegisterRequest` non ha il campo → ogni utente registrato avrà `dataNascita = null`.
Decidere se aggiungerlo al DTO di registrazione (e al form del frontend).

### Priorità 2 — Sicurezza

**2.1 L'endpoint interno è pubblico**
`/api/auth/internal/**` è `permitAll` (D9=a) **ed è raggiungibile dal gateway pubblico**
(`/api/auth/**` → auth-service). Chiunque conosca un UUID può leggere nome, cognome e data di
nascita di un utente.

Opzioni, in ordine di preferenza:
- **non esporre** il percorso `/internal/**` nel gateway (più pulito)
- proteggerlo con un token/segreto interno condiviso
- restringere a rete interna

### Priorità 3 — Qualità

**3.1 N+1 in `getAllBallerini`**
Per ogni ballerino il servizio fa una chiamata HTTP a auth-service. Intrinsico di D6=b.
Se il volume cresce: endpoint batch in auth-service o cache.

**3.2 Formato `dataNascita` divergente**
auth-service (Jackson 2) emette `"2000-01-15"`; ballerini-service (Jackson 3) emette
`"2000-01-15T00:00:00.000Z"`. È un comportamento **preesistente**, ma ora è visibile nel DTO
composto. Fix: `@JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")` su `BallerinoDto.dataNascita`.

**3.3 `BallerinoDto` non espone `sesso`**
Gap preesistente: `sesso` è nell'entità e nel `BallerinoCreate`, ma non nel DTO di risposta.

**3.4 `auth.service.url` non configurato**
`ballerini-service` si affida al default `http://localhost:7999`. In deployment va definito in
`application.yaml`.

**3.5 `deleteBallerino` legge l'anagrafica dopo aver cancellato**
Se l'utente è stato cancellato, la DELETE risponde 400. Margine, non bloccante.

### Priorità 4 — Differiti (già concordati)

**4.1 Completare `BallerinoService`**
`modificaBallerino()` ritorna `null`, `getBallerino()` è vuoto. Da fare quando si completa il
core dell'architettura.

**4.2 Le due entità `Presenza`**
`presenza-service` e `ballerini-service` hanno due `Presenza` con campi diversi. Da risolvere
come intervento separato.

### Priorità 5 — Operatività

**5.1 Push**
I commit sono **locali**. Il push su `develop` richiede le credenziali Gitea
(`192.168.1.22:4000`, non presenti in `~/.git-credentials`).

**5.2 Allineare `sql/schema-h2.sql`**
Lo spunto H2 non è stato aggiornato (il piano citava solo `schema.sql`). Follow-up di una riga.

---

## 6. Note

- Il lavoro è stato orchestrato con due agenti: uno per l'implementazione, uno per la verifica.
  La verifica ha confermato ogni affermazione dell'implementazione.
- Tutte le modifiche rispettano le decisioni prese; nessuna modifica non prevista.
- `presenza-service`, `gateway`, `eureka-service` e `BARD-Front` non sono stati toccati.
